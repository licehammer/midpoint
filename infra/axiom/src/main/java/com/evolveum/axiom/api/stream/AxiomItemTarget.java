package com.evolveum.axiom.api.stream;

import java.util.Optional;
import java.util.function.Supplier;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.AxiomItem;
import com.evolveum.axiom.api.AxiomItemBuilder;
import com.evolveum.axiom.api.AxiomValue;
import com.evolveum.axiom.api.AxiomValue.InfraFactory;
import com.evolveum.axiom.api.AxiomValueBuilder;
import com.evolveum.axiom.api.AxiomValueFactory;
import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.evolveum.axiom.api.schema.AxiomSchemaContext;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.evolveum.axiom.concepts.SourceLocation;
import com.evolveum.axiom.lang.spi.AxiomIdentifierResolver;
import com.google.common.base.Preconditions;

public class AxiomItemTarget<T extends AxiomValue<?>> extends AxiomBuilderStreamTarget implements Supplier<AxiomItem<T>>, AxiomItemStream.TargetWithResolver {

    private final AxiomSchemaContext context;
    private final AxiomIdentifierResolver resolver;
    private final AxiomValue.InfraFactory<?, T> factory;
    private Item<T> result;

    public AxiomItemTarget(AxiomSchemaContext context, AxiomIdentifierResolver resolver, AxiomValue.InfraFactory<?, T> factory) {
        offer(new Root());
        this.context = context;
        this.resolver = resolver;
        this.factory = factory;
    }

    @Override
    public AxiomItem<T> get() {
        return result.get();
    }

    private final class Root implements ValueBuilder {

        @Override
        public AxiomName name() {
            return AxiomName.axiom("AbstractRoot");
        }

        @Override
        public AxiomIdentifierResolver itemResolver() {
            return resolver;
        }

        @Override
        public AxiomIdentifierResolver valueResolver() {
            return resolver;
        }

        @Override
        public Optional<AxiomItemDefinition> childDef(AxiomName statement) {
            return context.getRoot(statement);
        }

        @Override
        public ItemBuilder startItem(AxiomName identifier, SourceLocation loc) {
            Preconditions.checkState(result == null, "Only one root item supported");
            result = new Item<>(childDef(identifier).get());
            return result;
        }

        @Override
        public void endValue(SourceLocation loc) {

        }

    }

    private final class Item<V extends AxiomValue<?>> implements ItemBuilder, Supplier<AxiomItem<V>> {

        private AxiomItemBuilder<V> builder;

        public Item(AxiomItemDefinition definition) {
            this.builder = new AxiomItemBuilder<>(definition);
        }

        @Override
        public AxiomName name() {
            return builder.definition().name();
        }

        @Override
        public AxiomIdentifierResolver itemResolver() {
            return resolver;
        }

        @Override
        public AxiomIdentifierResolver valueResolver() {
            return resolver;
        }

        @Override
        public ValueBuilder startValue(Object value, SourceLocation loc) {
            Value<V,T> newValue = new Value<>((V) value, builder.definition().typeDefinition(), factory);
            builder.addValue(newValue);
            return newValue;
        }

        @Override
        public void endNode(SourceLocation loc) {
            // Noop for now
        }

        @Override
        public AxiomItem<V> get() {
            return builder.get();
        }


    }

    private final class Value<V, T extends AxiomValue<V>> implements ValueBuilder, Supplier<T> {

        private final AxiomValueBuilder<V, T> builder;

        public Value(V value, AxiomTypeDefinition type, InfraFactory<V,T> factory) {
            builder = AxiomValueBuilder.from(type, factory);
            builder.setValue(value);
            if(value != null && type.argument().isPresent()) {
                AxiomItemDefinition argument = type.argument().get();
                startItem(argument.name(), null).startValue(value, null);
            } else {
                builder.setValue(value);
            }
        }

        @Override
        public AxiomName name() {
            return builder.type().name();
        }

        @Override
        public AxiomIdentifierResolver itemResolver() {
            return AxiomIdentifierResolver.defaultNamespaceFromType(builder.type());
        }

        @Override
        public AxiomIdentifierResolver valueResolver() {
            return resolver;
        }

        @Override
        public Optional<AxiomItemDefinition> childDef(AxiomName statement) {
            return builder.type().itemDefinition(statement);
        }

        @Override
        public ItemBuilder startItem(AxiomName identifier, SourceLocation loc) {
            Object itemImpl = builder.get(identifier, (id) -> {
                return new Item(childDef(identifier).get());
            });
            return (Item) (itemImpl);
        }

        @Override
        public void endValue(SourceLocation loc) {
            // Noop for now
        }

        @Override
        public T get() {
            return builder.get();
        }

    }
}
