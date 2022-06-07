/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.logging;

import org.apache.logging.log4j.Logger;

public class VitalsLoggingHelper {
    static public void info(Logger log, String msg, Object...parms) {
       log.info(msg, parms);
    }
}
