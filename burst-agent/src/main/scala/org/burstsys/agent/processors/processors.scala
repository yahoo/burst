/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent

import org.burstsys.vitals.logging.VitalsLogger

package object processors extends VitalsLogger {

  def singleLineSource(source: String): String = Predef.augmentString(source).linesIterator.map(_.trim.replace("\"", "'")).mkString("    ")

}
