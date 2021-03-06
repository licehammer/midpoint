/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.repo.sql.pure;

import java.time.Instant;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.ArrayPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;

import com.evolveum.midpoint.repo.sql.pure.mapping.QueryModelMapping;
import com.evolveum.midpoint.repo.sql.pure.mapping.QueryModelMappingConfig;

/**
 * Extension of {@link RelationalPathBase} that adapts the Q-class to midPoint
 * (mainly extension columns) while preserving all the features provided by Querydsl.
 * <p>
 * Typical migration from originally generated Querydsl class:
 * <ul>
 * <li>Extend from this class instead of {@code RelationalPathBase}.</li>
 * <li>Extract constants for all column metadata from {@code addMetadata()} method.
 * Remove index information from them (column order, nothing to do with DB indexes).</li>
 * <li>Rename the column name to conform with SQL Server (if still relevant), because it is
 * case-sensitive even about column names if *_CS_* collation is used!</li>
 * <li>Rewrite path fields so they use {@code create*} methods from this super-class.</li>
 * <li>Now {@code addMetadata()} method can be removed, including usages from constructors.</li>
 * <li>Prune constructors, two should be enough (see existing Q-classes).</li>
 * <li>Introduce {@code TABLE_NAME} constant - keep the names lowercase for MySQL (don't ask).</li>
 * <li>Rename path fields as needed (missing uppercase for words), also in related bean (M-class).</li>
 * <li>Unsuitable path types can be changed, e.g. date/time related.
 * The same changes must be done for the related field in the M-class.
 * Exotic type support can be added to Querydsl configuration, see static block in {@link SqlQueryExecutor}.</li>
 * <li>Remove default static final aliases, {@link QueryModelMapping} for the table will be
 * responsible for providing aliases, including default ones.
 * This better handles extension columns, static default alias would not easily know about them).</li>
 * <li>Simplify bean (M-class) to public fields with no setters/getters.</li>
 * <li>Add PK-based equals/hashCode to beans (not critical, but handy for grouping transformations).</li>
 * <li>Now it's time to create {@code Q_YourType_Mapping}, see any subclass of {@link QueryModelMapping}
 * as example, then register the mapping in {@link QueryModelMappingConfig}.</li>
 * </ul>
 *
 * @param <T> entity type - typically a pure DTO bean for the table mapped by Q-type
 */
public abstract class FlexibleRelationalPathBase<T> extends RelationalPathBase<T> {

    public static final String DEFAULT_SCHEMA_NAME = "PUBLIC";
    private static final long serialVersionUID = -3374516272567011096L;

    public FlexibleRelationalPathBase(
            Class<? extends T> type, PathMetadata metadata, String schema, String table) {
        super(type, metadata, schema, table);
    }

    /**
     * Creates {@link NumberPath} for a number property and registers column metadata for it.
     */
    protected <A extends Number & Comparable<?>> NumberPath<A> createNumber(
            String property, Class<A> type, ColumnMetadata columnMetadata) {
        return addMetadata(createNumber(property, type), columnMetadata);
    }

    /**
     * Creates {@link NumberPath} for an Integer property and registers column metadata for it.
     */
    protected NumberPath<Integer> createInteger(
            String property, ColumnMetadata columnMetadata) {
        return createNumber(property, Integer.class, columnMetadata);
    }

    /**
     * Creates {@link NumberPath} for a Long property and registers column metadata for it.
     */
    protected NumberPath<Long> createLong(
            String property, ColumnMetadata columnMetadata) {
        return createNumber(property, Long.class, columnMetadata);
    }

    /**
     * Creates {@link StringPath} and for a property registers column metadata for it.
     */
    protected StringPath createString(String property, ColumnMetadata columnMetadata) {
        return addMetadata(createString(property), columnMetadata);
    }

    /**
     * Creates {@link DateTimePath} for a property and registers column metadata for it.
     */
    @SuppressWarnings("rawtypes")
    protected <A extends Comparable> DateTimePath<A> createDateTime(
            String property, Class<? super A> type, ColumnMetadata columnMetadata) {
        return addMetadata(createDateTime(property, type), columnMetadata);
    }

    /**
     * Creates {@link DateTimePath} for an {@link Instant} property
     * and registers column metadata for it.
     */
    protected DateTimePath<Instant> createInstant(
            String property, ColumnMetadata columnMetadata) {
        return createDateTime(property, Instant.class, columnMetadata);
    }

    /**
     * Creates BLOB path for a property and registers column metadata for it.
     */
    protected ArrayPath<byte[], Byte> createBlob(
            String property, ColumnMetadata columnMetadata) {
        return addMetadata(createArray(property, byte[].class), columnMetadata);
    }
}
