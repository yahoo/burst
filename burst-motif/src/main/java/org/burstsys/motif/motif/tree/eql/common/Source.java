/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.tree.eql.common;

import org.burstsys.motif.common.Node;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.motif.tree.values.ValueExpression;
import org.burstsys.motif.symbols.Definition;
import org.burstsys.motif.symbols.PathSymbols;

import java.util.List;

public interface Source extends Node, Evaluation {

    String getDeclaredName();

    String getName();

    List<ValueExpression> getParms();

    String generateMotif(int level);

    Definition register(PathSymbols pathSymbols);
}
