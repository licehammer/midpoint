package com.evolveum.axiom.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.google.common.collect.Iterables;

public interface AxiomItem<T extends AxiomValue<?>> {

    AxiomName name();
    Optional<AxiomItemDefinition> definition();

    Collection<T> values();

    default T onlyValue() {
        return Iterables.getOnlyElement(values());
    }

    /*
    static <V> AxiomItem<V> of(AxiomItemDefinition def, V value) {
        return CompactAxiomItem.of(def, value);
    }*/

    static <T extends AxiomValue<?>> AxiomItem<T> from(AxiomItemDefinition def, Collection<? extends T> values) {
        return AxiomItemImpl.from(def, values);
    }

    static <T extends AxiomValue<?>> AxiomItem<T> from(AxiomItemDefinition def, T value) {
        return AxiomItemImpl.from(def, Collections.singleton(value));
    }


}
