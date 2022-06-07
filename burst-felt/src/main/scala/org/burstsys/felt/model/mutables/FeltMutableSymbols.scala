/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.mutables

import org.burstsys.felt.model.tree.code.FeltCode

trait FeltMutableSymbols {

  final private val valSetBinding = "feltBinding.mutables.valset"

  final lazy val grabValSetMethod: FeltCode = s"$valSetBinding.grabMutable"

  final lazy val releaseValSetMethod: FeltCode = s"$valSetBinding.releaseMutable"

  final private val valArrBinding = "feltBinding.mutables.valarr"

  final lazy val grabValArrMethod: FeltCode = s"$valArrBinding.grabMutable"

  final lazy val releaseValArrMethod: FeltCode = s"$valArrBinding.releaseMutable"

  final private val valMapBinding = "feltBinding.mutables.valmap"

  final lazy val grabValMapMethod: FeltCode = s"$valMapBinding.grabMutable"

  final lazy val releaseValMapMethod: FeltCode = s"$valMapBinding.releaseMutable"


}
