/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu

import org.burstsys.brio.runtime.BrioThreadRuntime
import org.burstsys.ginsu.functions.GinsuFunctions
import org.burstsys.ginsu.runtime.coerce.GinsuCoerceRuntime
import org.burstsys.ginsu.runtime.datetime.{GinsuDurationRuntime, GinsuGrainRuntime, GinsuOrdinalRuntime, GinsuTickRuntime}
import org.burstsys.ginsu.runtime.group.{GinsuEnumRuntime, GinsuSplitRuntime}

package object runtime {

  /**
   * the runtime extension for [[org.burstsys.ginsu.functions.GinsuFunctions]]
   * this converts the implicit threadRuntime to a object provided by the implementing
   * class
   */
  trait GinsuRuntime extends GinsuFunctions

    with GinsuTickRuntime with GinsuEnumRuntime with GinsuCoerceRuntime
    with GinsuSplitRuntime with GinsuGrainRuntime with GinsuDurationRuntime
    with GinsuOrdinalRuntime {

    implicit def threadRuntime: BrioThreadRuntime

  }


}
