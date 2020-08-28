/*
 * Copyright (c) 2018-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.test.asserter;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.Containerable;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.test.asserter.prism.PrismContainerValueAsserter;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValueMetadataType;

public class ValueMetadataValueAsserter<RA extends AbstractAsserter> extends PrismContainerValueAsserter<ValueMetadataType, RA> {

    public ValueMetadataValueAsserter(PrismContainerValue<ValueMetadataType> valueMetadataValue, RA parentAsserter, String details) {
        super(valueMetadataValue, parentAsserter, details);
    }

    @Override
    public ValueMetadataValueAsserter<RA> assertSize(int expected) {
        return (ValueMetadataValueAsserter<RA>) super.assertSize(expected);
    }

    @Override
    public ValueMetadataValueAsserter<RA> assertItems(QName... expectedItems) {
        return (ValueMetadataValueAsserter<RA>) super.assertItems(expectedItems);
    }

    @Override
    public ValueMetadataValueAsserter<RA> assertAny() {
        return (ValueMetadataValueAsserter<RA>) super.assertAny();
    }

    @Override
    public <T> ValueMetadataValueAsserter<RA> assertPropertyValuesEqual(ItemPath path, T... expectedValues) {
        return (ValueMetadataValueAsserter<RA>) super.assertPropertyValuesEqual(path, expectedValues);
    }

    @Override
    public <T> ValueMetadataValueAsserter<RA> assertPropertyValuesEqualRaw(ItemPath path, T... expectedValues) {
        return (ValueMetadataValueAsserter<RA>) super.assertPropertyValuesEqualRaw(path, expectedValues);
    }

    @Override
    public <T> ValueMetadataValueAsserter<RA> assertNoItem(ItemPath itemName) {
        return (ValueMetadataValueAsserter<RA>) super.assertNoItem(itemName);
    }

    public ProvenanceMetadataAsserter<ValueMetadataValueAsserter<RA>> provenance() {
        return new ProvenanceMetadataAsserter<>(getPrismValue().asContainerable().getProvenance(),
                this, "provenance in " + getDetails());
    }

    @Override
    public <CC extends Containerable> PrismContainerValueAsserter<CC, ValueMetadataValueAsserter<RA>> containerSingle(QName subcontainerQName) {
        return (PrismContainerValueAsserter<CC, ValueMetadataValueAsserter<RA>>) super.containerSingle(subcontainerQName);
    }


    protected String desc() {
        // TODO handling of details
        return "metadata of " + getDetails();
    }

    @Override
    public ValueMetadataValueAsserter<RA> display() {
        return (ValueMetadataValueAsserter<RA>) super.display();
    }
}
