/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.repo.sql.pure.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.namespace.QName;

import com.querydsl.core.types.EntityPath;

/**
 * Holds {@link QueryModelMapping} instances obtainable by various key (e.g. schema type Q-name).
 * The registry is oblivious to the actual configuration that is in {@link QueryModelMappingConfig}.
 */
public class QueryModelMappingRegistry {

    private final Map<Class<? extends EntityPath<?>>, QueryModelMapping<?, ?, ?>>
            mappingByQueryType = new HashMap<>();

    private final Map<String, QueryModelMapping<?, ?, ?>>
            mappingByDefaultAliasNames = new HashMap<>();

    private final Map<QName, QueryModelMapping<?, ?, ?>> mappingBySchemaQName = new HashMap<>();
    private final Map<Class<?>, QueryModelMapping<?, ?, ?>> mappingBySchemaType = new HashMap<>();

    /**
     * Register mapper bound to a schema type.
     * Mapping can be later obtained by its schema class, schema name or query class.
     */
    public QueryModelMappingRegistry register(
            QName schemaQName, QueryModelMapping<?, ?, ?> mapping) {

        QueryModelMapping<?, ?, ?> existingMapping = mappingBySchemaQName.get(schemaQName);
        if (existingMapping != null) {
            throw new IllegalArgumentException(
                    "New mapping tries to override schema QName '" + schemaQName + "': "
                            + mapping + "\nExisting mapping: " + existingMapping);
        }

        existingMapping = mappingBySchemaType.get(mapping.schemaType());
        if (existingMapping != null) {
            throw new IllegalArgumentException(
                    "New mapping tries to override schema type: " + mapping
                            + "\nExisting mapping: " + existingMapping);
        }

        // This order assures that all the checks are called before the first change
        // so if exception is thrown internal state of the registry is not corrupted.
        register(mapping);

        mappingBySchemaQName.put(schemaQName, mapping);
        mappingBySchemaType.put(mapping.schemaType(), mapping);

        return this;
    }

    /**
     * Register mapper not bound to a schema type.
     * This can happen for detail tables that have no unique mapping from schema type.
     * Mapping can be later obtained only by its query class, not by schema type/name.
     */
    public QueryModelMappingRegistry register(QueryModelMapping<?, ?, ?> mapping) {
        QueryModelMapping<?, ?, ?> existingMapping = mappingByQueryType.get(mapping.queryType());
        if (existingMapping != null) {
            throw new IllegalArgumentException(
                    "New mapping tries to override query type: " + mapping
                            + "\nExisting mapping: " + existingMapping);
        }
        existingMapping = mappingByDefaultAliasNames.get(mapping.defaultAliasName());
        if (existingMapping != null) {
            throw new IllegalArgumentException(
                    "New mapping tries to override default alias name: " + mapping
                            + "\nExisting mapping: " + existingMapping);
        }
        mappingByQueryType.put(mapping.queryType(), mapping);
        mappingByDefaultAliasNames.put(mapping.defaultAliasName(), mapping);

        return this;
    }

    public <S, Q extends EntityPath<R>, R>
    QueryModelMapping<S, Q, R> getBySchemaType(Class<S> schemaType) {
        //noinspection unchecked
        return (QueryModelMapping<S, Q, R>) Objects.requireNonNull(
                mappingBySchemaType.get(schemaType),
                () -> "Missing mapping for schema type " + schemaType);
    }

    public <S, Q extends EntityPath<R>, R>
    QueryModelMapping<S, Q, R> getByQueryType(Class<Q> queryType) {
        //noinspection unchecked
        return (QueryModelMapping<S, Q, R>) Objects.requireNonNull(
                mappingByQueryType.get(queryType),
                () -> "Missing mapping for query type " + queryType);
    }
}
