/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.motif.parser;

import org.burstsys.motif.Metadata;
import org.burstsys.motif.motif.tree.eql.queries.Query;
import org.burstsys.motif.motif.tree.eql.common.Statements;
import org.burstsys.motif.motif.tree.expression.Expression;
import org.burstsys.motif.motif.tree.view.View;

@Deprecated
public interface MotifParser {
    public Expression parseExpression(Metadata metadata, String schemaName, String expressionSource);
    public View parseView(Metadata metadata, String schemaName, String viewSource);
    public View parseView(Metadata metadata, String schemaName, String viewSource, String defaultTimeZoneName);
    public Query parseMotifQuery(Metadata metadata, String source);
    public Statements parseMotifStatements(Metadata metadata, String source);
}
