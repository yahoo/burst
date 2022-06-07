/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.felt.model.sweep.FeltSweepComponent
import org.burstsys.felt.model.sweep.runtime.FeltRuntimeComponent

package object dictionary {

  /**
   * Sweep code generated routines
   */
  trait FeltDictSweep extends Any with FeltSweepComponent

  /**
   * Sweep runtime code generated routines
   */
  trait FeltDictRuntime extends Any with FeltRuntimeComponent {

    /**
     * write a dictionary into a frame/plane at runtime
     *
     * @param frameId
     * @param dictionary
     */
    def frameDictionary(frameId: Int, dictionary: BrioMutableDictionary): Unit

    /**
     * access a dictionary from a frame/plane at runtime
     *
     * @param frameId
     * @return
     */
    def frameDictionary(frameId: Int): BrioMutableDictionary

  }

}
