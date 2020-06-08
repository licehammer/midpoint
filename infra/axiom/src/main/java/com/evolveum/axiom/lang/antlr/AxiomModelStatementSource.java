/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.axiom.lang.antlr;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.stream.AxiomItemStream;
import com.evolveum.axiom.lang.antlr.AxiomParser.DataItemContext;
import com.evolveum.axiom.lang.antlr.AxiomParser.ItemContext;
import com.evolveum.axiom.lang.spi.AxiomIdentifierResolver;
import com.evolveum.axiom.lang.spi.AxiomSyntaxException;

public class AxiomModelStatementSource extends AxiomAntlrStatementSource implements AxiomIdentifierResolver {

    private static final String IMPORT = "import";
    private static final String NAMESPACE = "namespace";

    private String name;
    private Map<String,String> imports;
    private String namespace;


    private AxiomModelStatementSource(String sourceName, DataItemContext statement, String namespace, String name, Map<String, String> imports) {
        super(sourceName, statement);
        this.name = name;
        this.imports = imports;
        this.namespace = namespace;
    }

    public static AxiomModelStatementSource from(InputStream stream) throws IOException, AxiomSyntaxException {
        return from(null, CharStreams.fromStream(stream));
    }

    public static AxiomModelStatementSource from(String sourceName, InputStream stream) throws IOException, AxiomSyntaxException {
        return from(sourceName, CharStreams.fromStream(stream));
    }

    public static AxiomModelStatementSource from(String sourceName, CharStream stream) throws AxiomSyntaxException {
        DataItemContext root = AxiomAntlrStatementSource.contextFrom(sourceName, stream);
        String name = root.item().value().argument().identifier().localIdentifier().getText();
        return new AxiomModelStatementSource(sourceName, root, name, namespace(root.item().value()), imports(root.item().value()));
    }

    public String modelName() {
        return name;
    }

    public String namespace() {
        return namespace;
    }


    public void stream(AxiomIdentifierResolver resolver, AxiomItemStream.Target listener,
            Optional<Set<AxiomName>> emitOnly) {
        stream(resolver.or(this), BUILTIN_TYPES.or(this).or(resolver), listener, emitOnly);
    }

    public Map<String, String> imports() {
        return imports;
    }

    public static Map<String,String> imports(AxiomParser.ValueContext root) {
        Map<String,String> prefixMap = new HashMap<>();
        root.dataItem().stream().filter(s -> IMPORT.equals(s.item().identifier().getText())).forEach(c -> {
            String prefix = c.item().value().argument().identifier().localIdentifier().getText();
            String namespace = namespace(c.item().value());
            prefixMap.put(prefix, namespace);
        });
        prefixMap.put("",namespace(root));
        return prefixMap;
    }

    private static String namespace(AxiomParser.ValueContext c) {
        return AxiomAntlrVisitor.convert(c.dataItem()
                .stream().filter(s -> NAMESPACE.equals(s.item().identifier().getText()))
                .findFirst().get().item().value().argument().string());
    }

    @Override
    public AxiomName resolveIdentifier(@Nullable String prefix, @NotNull String localName) {
        if(prefix == null) {
            prefix = "";
        }
        String maybeNs = imports.get(prefix);
        if(maybeNs != null) {
            return AxiomName.from(maybeNs, localName);
        }
        return null;
    }
}
