/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube

import org.burstsys.felt.model.collectors.runtime.FeltCollectorFactory
import org.burstsys.felt.model.sweep.runtime.FeltRuntimeComponent

package object runtime {

  trait FeltCubeRow extends Any

  /**
   * Sweep runtime code generated routines for cube processing
   */
  trait FeltCubeRuntime extends Any with FeltRuntimeComponent

  trait FeltCubeFactory extends Any with FeltCollectorFactory[FeltCubeBuilder, FeltCubeCollector]

}
