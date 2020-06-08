package com.evolveum.axiom.api;

import com.evolveum.axiom.concepts.Lazy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class AxiomValueInfraBuilder<V,T extends AxiomValue<V>> implements Lazy.Supplier<T> {

    private AxiomValue.InfraFactory<V,T> factory;
    private Map<AxiomName, Supplier<? extends AxiomItem<?>>> children = new LinkedHashMap<>();

    protected AxiomValueInfraBuilder(AxiomValue.InfraFactory<V,T> factory) {
        this.factory = factory;
    }

    public static <V,T extends AxiomValue<V>> AxiomValueInfraBuilder<V,T> from(AxiomValue.InfraFactory<V,T> type) {
        return new AxiomValueInfraBuilder<>(type);
    }

    public void addInfra(AxiomName name, Supplier<? extends AxiomItem<?>> child) {
        children.put(name, child);
    }

    public Supplier<? extends AxiomItem<?>> getInfra(AxiomName name) {
        return children.get(name);
    }

    public Supplier<? extends AxiomItem<?>> getInfra(AxiomName name, Function<AxiomName, ? extends Supplier<? extends AxiomItem<?>>> child) {
        return children.computeIfAbsent(name, child);
    }

    @Override
    public T get() {
        Builder<AxiomName, AxiomItem<?>> builder = ImmutableMap.builder();
        for(Entry<AxiomName, Supplier<? extends AxiomItem<?>>> entry : children.entrySet()) {
            AxiomItem<?> item = entry.getValue().get();
            builder.put(entry.getKey(), entry.getValue().get());
        }
        return factory.create(builder.build());
    }

}
