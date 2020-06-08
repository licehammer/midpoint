package com.evolveum.axiom.api;

import java.util.Collection;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;

public interface AxiomItemFactory<V extends AxiomValue<?>> {

    AxiomItem<V> create(AxiomItemDefinition def, Collection<? extends V> axiomItem);

}
