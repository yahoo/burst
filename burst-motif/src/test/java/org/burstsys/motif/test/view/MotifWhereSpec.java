/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.view;

import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.test.MotifAbstractParserSpec;
import org.junit.Before;
import org.junit.Test;

public class MotifWhereSpec extends MotifAbstractParserSpec {

    @Before
    public void init() {
        schemaName = "unity";
    }

    @Test
    public void offAxisTest() {
        String source = "view \"sample\" {\n" +
                "INCLUDE user WHERE user.sessions.startTime == 1 AND user.interests == 2" +
                "}";
        View result = runViewTest(source);
        assert result != null;
    }

    @Test
    public void offAxisTest2() {
        String source = "view \"sample\" {\n" +
                "INCLUDE user WHERE user.sessions.startTime != 1 AND user.interests == 2" +
                "}";
        View result = runViewTest(source);
        assert result != null;
    }

}
