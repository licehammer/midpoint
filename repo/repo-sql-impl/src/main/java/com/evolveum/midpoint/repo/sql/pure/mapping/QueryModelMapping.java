/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.repo.sql.pure.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.xml.namespace.QName;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.ColumnMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.repo.sql.pure.FilterProcessor;
import com.evolveum.midpoint.repo.sql.pure.SqlPathContext;
import com.evolveum.midpoint.repo.sql.pure.SqlTransformer;
import com.evolveum.midpoint.repo.sql.query.QueryException;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SystemException;

/**
 * Common supertype for mapping items/attributes between schema (prism) classes and query types.
 * See {@link #addItemMapping(ItemName, ItemSqlMapper)} for details about mapping mechanism.
 * See {@link #addDetailFetchMapper(ItemName, SqlDetailFetchMapper)} for more about mapping
 * related to-many detail tables.
 * <p>
 * <b>Goal:</b> Map object query conditions and ORDER BY to SQL.
 * <p>
 * <b>Non-goal:</b> Map objects from Q-type to prism and back.
 * This is done by code, possibly static method from a DTO "assembler" class.
 *
 * @param <S> schema type
 * @param <Q> type of entity path
 * @param <R> row type related to the {@link Q}
 */
public abstract class QueryModelMapping<S, Q extends EntityPath<R>, R> {

    private final String tableName;
    private final String defaultAliasName;
    private final Class<S> schemaType;
    private final Class<Q> queryType;

    // TODO MID-6319: support for extension columns + null default alias after every change (in synchronized block)
    private final Map<String, ColumnMetadata> columns = new LinkedHashMap<>();

    private final Map<QName, ItemSqlMapper> itemFilterProcessorMapping = new LinkedHashMap<>();
    private final Map<QName, SqlDetailFetchMapper<R, ?, ?, ?>> detailFetchMappers = new HashMap<>();

    private Q defaultAlias;

    /**
     * Creates metamodel for the table described by designated type (Q-class) related to schema type.
     * Allows registration of any number of columns - typically used for static properties
     * (non-extensions).
     *
     * @param tableName database table name
     * @param defaultAliasName default alias name, some short abbreviation, must be unique
     * across mapped types
     */
    protected QueryModelMapping(
            @NotNull String tableName,
            @NotNull String defaultAliasName,
            @NotNull Class<S> schemaType,
            @NotNull Class<Q> queryType,
            ColumnMetadata... columns) {
        this.tableName = tableName;
        this.defaultAliasName = defaultAliasName;
        this.schemaType = schemaType;
        this.queryType = queryType;
        for (ColumnMetadata column : columns) {
            this.columns.put(column.getName(), column);
        }
    }

    public final QueryModelMapping<S, Q, R> add(ColumnMetadata column) {
        columns.put(column.getName(), column);
        return this;
    }

    /**
     * Adds information how item (attribute) from schema type is mapped to query,
     * especially for condition creating purposes.
     * <p>
     * The {@link ItemSqlMapper} works as a factory for {@link FilterProcessor} that can process
     * {@link ObjectFilter} related to the {@link ItemName} specified as the first parameter.
     * It is not possible to use filter processor directly because at the time of mapping
     * specification we don't have the actual query path representing the entity or the column.
     * These paths are non-static properties of query class instances.
     * <p>
     * The {@link ItemSqlMapper} also provides so called "primary mapping" to a column for ORDER BY
     * part of the filter.
     * But there can be additional column mappings specified as for some types (e.g. poly-strings)
     * there may be other than 1-to-1 mapping.
     * <p>
     * Construction of the {@link ItemSqlMapper} is typically simplified by static methods
     * {@code #mapper()} provided on various {@code *ItemFilterProcessor} classes.
     * This works as a "processor factory factory" and makes table mapping specification simpler.
     *
     * @param itemName item name from schema type (see {@code F_*} constants on schema types)
     * @param itemMapper mapper wrapping the information about column mappings working also
     * as a factory for {@link FilterProcessor}
     */
    public final void addItemMapping(ItemName itemName, ItemSqlMapper itemMapper) {
        itemFilterProcessorMapping.put(itemName, itemMapper);
    }

    /**
     * Fetcher/mappers for detail tables take care of loading to-many details related to
     * this mapped entity (master).
     * One fetcher per detail type/table is registered under the related item name.
     *
     * @param itemName item name from schema type that is mapped to detail table in the repository
     * @param detailFetchMapper fetcher-mapper that handles loading of details
     * @see SqlDetailFetchMapper
     */
    public final void addDetailFetchMapper(
            ItemName itemName,
            SqlDetailFetchMapper<R, ?, ?, ?> detailFetchMapper) {
        detailFetchMappers.put(itemName, detailFetchMapper);
    }

    /**
     * Lambda "wrapper" that helps with type inference when mapping paths from entity path.
     * The returned types are ambiguous just as they are used in {@code mapper()} static methods
     * on item filter processors.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <A> Function<EntityPath<?>, Path<?>> path(
            Function<Q, Path<A>> rootToQueryItem) {
        return (Function) rootToQueryItem;
    }

    /**
     * Lambda "wrapper" but this time with explicit query type (otherwise unused by the method)
     * for paths not starting on the Q parameter used for this mapping instance.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <OQ extends EntityPath<OR>, OR, A> Function<EntityPath<?>, Path<?>> path(
            @SuppressWarnings("unused") Class<OQ> queryType,
            Function<OQ, Path<A>> entityToQueryItem) {
        return (Function) entityToQueryItem;
    }

    /**
     * Helping lambda "wrapper" that helps with the type inference (namely the current Q type).
     */
    protected <DQ extends EntityPath<DR>, DR> BiFunction<Q, DQ, Predicate> joinOn(
            BiFunction<Q, DQ, Predicate> joinOnPredicateFunction) {
        return joinOnPredicateFunction;
    }

    // we want loose typing for client's sake, there is no other chance to get the right type here
    public final <T extends ObjectFilter> @NotNull FilterProcessor<T> createItemFilterProcessor(
            ItemName itemName, SqlPathContext<?, ?, ?> context)
            throws QueryException {
        return itemMapping(itemName).createFilterProcessor(context);
    }

    public final @Nullable Path<?> primarySqlPath(ItemName itemName, SqlPathContext<?, ?, ?> context)
            throws QueryException {
        return itemMapping(itemName).itemPath(context.path());
    }

    public final @NotNull ItemSqlMapper itemMapping(ItemName itemName) throws QueryException {
        ItemSqlMapper itemMapping = QNameUtil.getByQName(itemFilterProcessorMapping, itemName);
        if (itemMapping == null) {
            throw new QueryException("Missing mapping for " + itemName
                    + " in mapping " + getClass().getSimpleName());
        }
        return itemMapping;
    }

    public String tableName() {
        return tableName;
    }

    public String defaultAliasName() {
        return defaultAliasName;
    }

    /**
     * This refers to midPoint schema, not DB schema.
     */
    public Class<S> schemaType() {
        return schemaType;
    }

    public Class<Q> queryType() {
        return queryType;
    }

    public Q newAlias(String alias) {
        try {
            return queryType.getConstructor(String.class).newInstance(alias);
        } catch (ReflectiveOperationException e) {
            throw new SystemException("Invalid constructor for type " + queryType, e);
        }
    }

    public synchronized Q defaultAlias() {
        if (defaultAlias == null) {
            defaultAlias = newAlias(defaultAliasName);
        }
        return defaultAlias;
    }

    /**
     * Creates {@link SqlTransformer} of row bean to schema type.
     */
    public abstract SqlTransformer<S, R> createTransformer(PrismContext prismContext);

    /**
     * Returns collection of all registered {@link SqlDetailFetchMapper}s.
     */
    public final Collection<SqlDetailFetchMapper<R, ?, ?, ?>> detailFetchMappers() {
        return detailFetchMappers.values();
    }

    /**
     * Returns {@link SqlDetailFetchMapper} registered for the specified {@link ItemName}.
     */
    public final SqlDetailFetchMapper<R, ?, ?, ?> detailFetchMapper(ItemName itemName) {
        return detailFetchMappers.get(itemName);
    }

    @Override
    public String toString() {
        return "QueryModelMapping{" +
                "tableName='" + tableName + '\'' +
                ", defaultAliasName='" + defaultAliasName + '\'' +
                ", schemaType=" + schemaType +
                ", queryType=" + queryType +
                '}';
    }
}
