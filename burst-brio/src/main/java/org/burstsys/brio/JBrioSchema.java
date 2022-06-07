/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio;

abstract public class JBrioSchema {
    abstract public JBrioSchematic getRoot();
    abstract public JBrioSchematic getSchematic(byte typeKey);
}
