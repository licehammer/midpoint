package com.evolveum.axiom.api;

import java.util.Map;
import java.util.Optional;

import com.evolveum.axiom.api.schema.AxiomTypeDefinition;


public interface AxiomValue<V> {

    AxiomName TYPE = AxiomName.axiom("type");
    AxiomName VALUE = AxiomName.axiom("value");

    Optional<AxiomTypeDefinition> type();

    V value();

    default <C> C value(Class<C> type) {
        V value = value();
        if(type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    default Optional<AxiomComplexValue> asComplex() {
        if(this instanceof AxiomComplexValue)  {
            return Optional.of((AxiomComplexValue) this);
        }
        return Optional.empty();
    }

    default Optional<AxiomItem<?>> infraItem(AxiomName name) {
        return Optional.empty();
    }

    interface InfraFactory<V, T extends AxiomValue<V>> {

        default T create(Map<AxiomName, AxiomItem<?>> infraItems) {
            AxiomTypeDefinition type = infraItems.get(TYPE).onlyValue().value(AxiomTypeDefinition.class);
            V value = (V) infraItems.get(VALUE).onlyValue().value();
            return create(type, value, infraItems);
        }

        T create(AxiomTypeDefinition valueType, V value, Map<AxiomName,AxiomItem<?>> infraItems);
    }

}
