/*
 * Copyright (c) 2018-2019 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.test.asserter;

import static org.assertj.core.api.Assertions.assertThat;

import static com.evolveum.midpoint.schema.util.ProvenanceMetadataUtil.hasOrigin;

import java.util.List;
import java.util.stream.Collectors;

import com.evolveum.midpoint.test.asserter.prism.PrismContainerValueAsserter;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ProvenanceAcquisitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ProvenanceYieldType;

public class YieldMetadataAsserter<RA extends AbstractAsserter> extends PrismContainerValueAsserter<ProvenanceYieldType, RA> {

    YieldMetadataAsserter(
            ProvenanceYieldType yield, RA returnAsserter, String detail) {
        //noinspection unchecked
        super(yield.asPrismContainerValue(), returnAsserter, detail);
    }

    public AcquisitionMetadataAsserter<YieldMetadataAsserter<RA>> singleAcquisition() {
        return new AcquisitionMetadataAsserter<>(getSingleAcquisition(), this, "acquisition in " + getDetails());
    }

    public AcquisitionMetadataAsserter<YieldMetadataAsserter<RA>> singleAcquisition(String originOid) {
        return new AcquisitionMetadataAsserter<>(getSingleAcquisition(originOid), this, "acquisition in " + getDetails());
    }

    private ProvenanceAcquisitionType getSingleAcquisition() {
        List<ProvenanceAcquisitionType> acquisitions = getPrismValue().asContainerable().getAcquisition();
        assertThat(acquisitions.size()).as("# of acquisitions in " + getDetails())
                .isEqualTo(1);
        return acquisitions.get(0);
    }

    private ProvenanceAcquisitionType getSingleAcquisition(String originOid) {
        List<ProvenanceAcquisitionType> acquisitions = getPrismValue().asContainerable().getAcquisition().stream()
                .filter(acquisition -> hasOrigin(acquisition, originOid))
                .collect(Collectors.toList());
        assertThat(acquisitions.size()).as("# of acquisitions with origin " + originOid + " in " + getDetails())
                .isEqualTo(1);
        return acquisitions.get(0);
    }
}
