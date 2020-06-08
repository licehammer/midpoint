/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.axiom.lang.antlr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.stream.AxiomItemStream;
import com.evolveum.axiom.lang.antlr.AxiomParser.DataItemContext;
import com.evolveum.axiom.lang.antlr.AxiomParser.ItemContext;
import com.evolveum.axiom.lang.spi.AxiomIdentifierResolver;
import com.evolveum.axiom.lang.spi.AxiomSyntaxException;

public class AxiomAntlrStatementSource {

    private final DataItemContext root;
    private final String sourceName;

    protected AxiomAntlrStatementSource(String sourceName, DataItemContext statement) {
        this.sourceName = sourceName;
        this.root = statement;
    }

    public static AxiomAntlrStatementSource from(String sourceName, InputStream stream) throws IOException, AxiomSyntaxException {
        return from(sourceName, CharStreams.fromStream(stream));
    }

    public static DataItemContext contextFrom(String sourceName, CharStream stream) {
        AxiomLexer lexer = new AxiomLexer(stream);
        AxiomParser parser = new AxiomParser(new CommonTokenStream(lexer));
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        AxiomErrorListener errorListener = new AxiomErrorListener(sourceName);
        parser.addErrorListener(errorListener);
        DataItemContext statement = parser.dataItem();
        errorListener.validate();
        return statement;
    }

    public static AxiomAntlrStatementSource from(String sourceName, CharStream stream) throws AxiomSyntaxException {
        DataItemContext statement = contextFrom(sourceName, stream);
        return new AxiomAntlrStatementSource(sourceName, statement);
    }

    public String sourceName() {
        return sourceName;
    }

    protected final DataItemContext root() {
        return root;
    }

    public final void stream(AxiomItemStream.TargetWithResolver target) {
        stream(target, Optional.empty());
    }

    public final void stream(AxiomItemStream.TargetWithResolver target, Optional<Set<AxiomName>> emitOnly) {
        AxiomAntlrVisitor2<?> visitor = new AxiomAntlrVisitor2<>(sourceName, target, emitOnly.orElse(null));
        visitor.visit(root);
    }

    public final void stream(AxiomIdentifierResolver statements, AxiomIdentifierResolver arguments, AxiomItemStream.Target listener,
            Optional<Set<AxiomName>> emitOnly) {
        AxiomAntlrVisitor<?> visitor = new AxiomAntlrVisitor<>(sourceName, statements, arguments, listener, emitOnly.orElse(null));
        visitor.visit(root);
    }

}
