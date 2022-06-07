/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.model;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.logging.log4j.Logger;

public class BrioParserErrorListener extends BaseErrorListener {

    private Logger logger = null;

    public BrioParserErrorListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {
        logger.error("line " + line + ":" + charPositionInLine + " " + msg);
    }

}
