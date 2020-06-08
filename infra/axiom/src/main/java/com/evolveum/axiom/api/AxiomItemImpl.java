package com.evolveum.axiom.api;

import java.util.Collection;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.google.common.collect.ImmutableList;

class AxiomItemImpl<T extends AxiomValue<?>> extends AbstractAxiomItem<T> {

    Collection<T> values;


    private AxiomItemImpl(AxiomItemDefinition definition, Collection<? extends T> val) {
        super(definition);
        this.values = ImmutableList.copyOf(val);
    }

    static <T extends AxiomValue<?>> AxiomItem<T> from(AxiomItemDefinition definition, Collection<? extends T> values) {
        return new AxiomItemImpl<>(definition, values);
    }

    @Override
    public Collection<T> values() {
        return values;
    }

}
