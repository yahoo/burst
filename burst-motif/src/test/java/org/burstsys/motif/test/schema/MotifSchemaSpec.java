/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.test.schema;

import org.junit.Test;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.test.MotifAbstractParserSpec;

public class MotifSchemaSpec extends MotifAbstractParserSpec {

    @Test(expected = ParseException.class)
    public void unquotedIdentifiersCollideWithKeywords() {
        String source = "schema Test { version:1 root)foo:Foo structure Foo { 0)bax:long 1)version:long } }";
        motif.parseSchema(source);
    }

    @Test
    public void noncollidingIdentifiersWork() {
        String source = "schema Test { version:1 root)foo:Foo structure Foo { 0)bax:long 1)varsion:long } }";
        motif.parseSchema(source);
    }

    @Test
    public void doubleQuotedIdentifiersWorkToo() {
        String source = "schema Test { version:1 root)foo:Foo structure Foo { 0)bax:long 1)\"version\":long } }";
        motif.parseSchema(source);
    }

    @Test
    public void singleQuotedIdentifiersWorkToo() {
        String source = "schema Test { version:1 root)foo:Foo structure Foo { 0)bax:long 1)'version':long } }";
        motif.parseSchema(source);
    }

    @Test(expected = ParseException.class)
    public void redundantFieldNumberError() {
        String source = "schema Test { version:1 root)foo:Foo structure Bar { 0)foo:Long 1)bar:String 1)baz:Double} }";
        motif.parseSchema(source);
    }

}
