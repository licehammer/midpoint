package com.evolveum.axiom.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class AxiomItemBuilder<V extends AxiomValue<?>> implements Supplier<AxiomItem<V>> {

    Collection<Supplier<? extends V>> values = new ArrayList<>();
    private AxiomItemDefinition definition;

    public AxiomItemBuilder(AxiomItemDefinition definition) {
        this.definition = definition;
    }

    public AxiomItemDefinition definition() {
        return definition;
    }

    public void addValue(Supplier<? extends V> value) {
        values.add(value);
    }

    @Override
    public AxiomItem<V> get() {
        Builder<V> result = ImmutableList.builder();
        for(Supplier<? extends V> value : values) {
            result.add(value.get());
        }
        return AxiomItem.from(definition, result.build());
    }

}
