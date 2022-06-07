/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test;

import org.burstsys.motif.motif.tree.rule.FilterRuleType;
import org.burstsys.motif.motif.tree.view.View;
import org.junit.Test;

public class MotifOperationsViewSpec extends MotifAbstractParserSpec {

    @Test
    public void mergeview1() {
        String expressionSource1 =
                "VIEW view1 {\n" +
                        "   INCLUDE user WHERE user.sessions.events.eventId in (17964296, 17964089)\n" +
                        "}";

        View v1 = motif.parseView(schemaName, expressionSource1);

        String expressionSource2 =
                "VIEW view2 {\n" +
                        "   INCLUDE user WHERE user.sessions.events.eventId in (17964297, 17964090)\n" +
                        "}";

        View v2 = motif.parseView(schemaName, expressionSource2);

        View vn = v1.unionView(v2);

        assert  vn.getRules().length == 1;
        assert vn.getRules()[0].getType() == FilterRuleType.INCLUDE;

        String newView = vn.generateMotif(0);
        assert newView != null;

        View result = runViewTest(newView);
        assert result != null;
    }

    @Test
    public void mergeview2() {
        String expressionSource1 =
                "VIEW \"view1\" {\n" +
                        "   INCLUDE user WHERE user.sessions.events.eventId in (17964296, 17964089)\n" +
                        "}";

        View v1 = motif.parseView(schemaName, expressionSource1);

        String expressionSource2 =
                "VIEW view2 {\n" +
                        "   INCLUDE user WHERE NOT(user.sessions.events.eventId in (17964297, 17964090))\n" +
                        "}";

        View v2 = motif.parseView(schemaName, expressionSource2);

        View vm = v1.intersectView(v2);

        assert  vm.getRules().length == 1;
        assert vm.getRules()[0].getType() == FilterRuleType.INCLUDE;

        String newView = vm.generateMotif(0);
        assert newView != null;

        View result = runViewTest(newView);
        assert result != null;
    }

    @Test
    public void mergeview4() {
        String expressionSource1 =
                "VIEW view1 {\n" +
                        "   INCLUDE user WHERE user.sessions.events.eventId in (17964296, 17964089)\n" +
                        "   INCLUDE user.sessions WHERE false\n" +
                        "}";

        View v1 = motif.parseView(schemaName, expressionSource1);

        String expressionSource2 =
                "VIEW view2 {\n" +
                        "   INCLUDE user WHERE NOT(user.sessions.events.eventId in (17964297, 17964090))\n" +
                        "}";

        View v2 = motif.parseView(schemaName, expressionSource2);

        View vm = v1.intersectView(v2);

        View result = runViewTest(vm.generateMotif(0));
        assert result != null;
    }

    @Test
    public void messagingMergeView1() {
        schemaName = "unity";
        String uiSource =
                "VIEW v { INCLUDE user WHERE user.application.lastUse.countryId in (435463,435465,435464) " +
                        " }";

        View v1 = motif.parseView("unity", uiSource);

        String messageSource =
                "VIEW v1 { INCLUDE user WHERE " +
                        "(user.application.lastUse.timeZoneOffsetSecs in (0)) && (user.application.lastUse.pushTokenStatus == 2) " +
                        "INCLUDE user.sessions WHERE false INCLUDE user.sessions.events WHERE false}";

        View v2 = motif.parseView("unity", messageSource);

        View vm = v1.intersectView(v2);

        View result = runViewTest(vm.generateMotif(0));
        assert result != null;
        assert result.getRules().length == 3;
    }

    @Test
    public void messagingMergeView2() {
        schemaName = "unity";
        String uiSource =
                "VIEW v { INCLUDE user WHERE ((count(user.sessions.events) where (user.sessions.events.id in (15781167))) >= 1) }";

        View v1 = motif.parseView("unity", uiSource);

        String messageSource =
                "VIEW v1 { INCLUDE user WHERE " +
                        "(user.application.lastUse.timeZoneOffsetSecs in (0)) && (user.application.lastUse.pushTokenStatus == 2) " +
                        "INCLUDE user.sessions WHERE false INCLUDE user.sessions.events WHERE false}";

        View v2 = motif.parseView("unity", messageSource);

        View vm = v1.intersectView(v2);

        View result = runViewTest(vm.generateMotif(0));
        assert result != null;
        assert result.getRules().length == 3;
    }
}
