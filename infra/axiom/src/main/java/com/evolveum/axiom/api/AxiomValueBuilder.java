package com.evolveum.axiom.api;

import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.evolveum.axiom.concepts.Lazy;
import com.evolveum.axiom.lang.impl.ItemValueImpl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class AxiomValueBuilder<V,T extends AxiomValue<V>> extends AxiomValueInfraBuilder<V, T> implements Lazy.Supplier<T> {

    private final AxiomTypeDefinition type;

    private AxiomValueFactory<V,T> factory;
    private Map<AxiomName, Supplier<? extends AxiomItem<?>>> dataItems = new LinkedHashMap<>();
    private V value;

    public AxiomValueBuilder(AxiomTypeDefinition type, AxiomValueFactory<V,T> factory) {
        super(null);
        this.type = type;
        this.factory = factory;
    }

    public static <V,T extends AxiomValue<V>> AxiomValueBuilder<V, T> from(AxiomTypeDefinition type, AxiomValue.InfraFactory<V, T> factory) {
        return new AxiomValueBuilder(type, type.isComplex() ? ItemValueImpl.factory() : SimpleValue.factory());
    }


    public static <V> AxiomValueBuilder<V, AxiomValue<V>> from(AxiomTypeDefinition type) {
        return new AxiomValueBuilder(type, type.isComplex() ? ItemValueImpl.factory() : SimpleValue.factory());
    }


    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void add(AxiomName name, Supplier<? extends AxiomItem<?>> child) {
        dataItems.put(name, child);
    }

    public Supplier<? extends AxiomItem<?>> get(AxiomName name) {
        return dataItems.get(name);
    }

    public Supplier<? extends AxiomItem<?>> get(AxiomName name, Function<AxiomName, ? extends Supplier<? extends AxiomItem<?>>> child) {
        return dataItems.computeIfAbsent(name, child);
    }

    @Override
    public T get() {
        if(type.isComplex()) {
            Builder<AxiomName, AxiomItem<?>> builder = ImmutableMap.builder();
            for(Entry<AxiomName, Supplier<? extends AxiomItem<?>>> entry : dataItems.entrySet()) {
                AxiomItem<?> item = entry.getValue().get();
                builder.put(entry.getKey(), entry.getValue().get());
            }
            return factory.create(type, null, builder.build());
        }
        Preconditions.checkState(dataItems.isEmpty(), "%s does not have items. Items found %s", type.name(), dataItems.keySet());
        return factory.create(type, value, Collections.emptyMap());
    }

    public static <T,V extends AxiomValue<T>> AxiomValueBuilder<T,V> create(AxiomTypeDefinition type, AxiomValueFactory<T, V> factory) {
        return new AxiomValueBuilder<>(type, factory);
    }

    public void setFactory(AxiomValueFactory<V,T> factoryFor) {
        this.factory = factoryFor;
    }

    public AxiomTypeDefinition type() {
        return type;
    }

}
