package com.evolveum.axiom.api;

import java.util.Optional;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;

public abstract class AbstractAxiomItem<T extends AxiomValue<?>> implements AxiomItem<T> {


    private final AxiomItemDefinition definition;

    public AbstractAxiomItem(AxiomItemDefinition definition) {
        this.definition = definition;
    }

    @Override
    public Optional<AxiomItemDefinition> definition() {
        return Optional.of(definition);
    }

    @Override
    public AxiomName name() {
        return definition.name();
    }
}
