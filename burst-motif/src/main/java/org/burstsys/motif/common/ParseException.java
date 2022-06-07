/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.common;

import org.antlr.v4.runtime.RecognitionException;

import static java.lang.String.format;

public class ParseException
        extends RuntimeException {
    private final int line;
    private final int charPositionInLine;

    public ParseException(String message, RecognitionException cause, int line, int charPositionInLine) {
        super(message, cause);

        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public ParseException(String message) {
        this(message, null, 1, 0);
    }

    public ParseException(NodeLocation nodeLocation, String message) {
        this(message, null, nodeLocation.getLineNumber(), nodeLocation.getColumnNumber());
    }

    public ParseException(NodeContext node, String message) {
        this(message, null, node.getLocation().getLineNumber(), node.getLocation().getColumnNumber());
    }

    public int getLineNumber() {
        return line;
    }

    public int getColumnNumber() {
        return charPositionInLine + 1;
    }

    public String getErrorMessage() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        return format("line %s:%s: %s", getLineNumber(), getColumnNumber(), getErrorMessage());
    }
}
