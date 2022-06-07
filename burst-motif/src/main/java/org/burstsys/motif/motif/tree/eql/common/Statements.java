/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common;

import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;

public interface Statements extends Evaluation {

    List<Statement> getStatements();

    Statements finalizeStatements(PathSymbols pathSymbols);

}
