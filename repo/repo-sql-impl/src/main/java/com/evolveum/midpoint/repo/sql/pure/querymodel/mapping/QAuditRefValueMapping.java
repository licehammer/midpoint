/*
 * Copyright (C) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.repo.sql.pure.querymodel.mapping;

import static com.evolveum.midpoint.repo.sql.pure.querymodel.QAuditItem.*;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.pure.SqlTransformer;
import com.evolveum.midpoint.repo.sql.pure.mapping.QueryModelMapping;
import com.evolveum.midpoint.repo.sql.pure.querymodel.QAuditRefValue;
import com.evolveum.midpoint.repo.sql.pure.querymodel.beans.MAuditRefValue;
import com.evolveum.midpoint.xml.ns._public.common.audit_3.AuditEventRecordReferenceType;

/**
 * Mapping between {@link QAuditRefValue} and {@link AuditEventRecordReferenceType}.
 */
public class QAuditRefValueMapping
        extends QueryModelMapping<AuditEventRecordReferenceType, QAuditRefValue, MAuditRefValue> {

    public static final String DEFAULT_ALIAS_NAME = "aref";

    public static final QAuditRefValueMapping INSTANCE = new QAuditRefValueMapping();

    private QAuditRefValueMapping() {
        super(TABLE_NAME, DEFAULT_ALIAS_NAME,
                AuditEventRecordReferenceType.class, QAuditRefValue.class,
                RECORD_ID, CHANGED_ITEM_PATH);
    }

    @Override
    public SqlTransformer<AuditEventRecordReferenceType, MAuditRefValue> createTransformer(
            PrismContext prismContext) {
        throw new UnsupportedOperationException("handled by AuditEventRecordSqlTransformer");
    }
}
