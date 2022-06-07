/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer

/**
  * captures all output functions of roll pressing
  */
trait BrioPressSink extends Any {

  /**
    * Writable Buffer to capture output
    * @return
    */
  def buffer: TeslaMutableBuffer

  /**
    * Writable dictionary to capture output
    * @return
    */
  def dictionary: BrioMutableDictionary

}

object BrioPressSink {

  def apply(buffer: TeslaMutableBuffer, dictionary: BrioMutableDictionary): BrioPressSink = BrioPressSinkContext(buffer, dictionary)

}

private final case
class BrioPressSinkContext(buffer: TeslaMutableBuffer, dictionary: BrioMutableDictionary) extends BrioPressSink
