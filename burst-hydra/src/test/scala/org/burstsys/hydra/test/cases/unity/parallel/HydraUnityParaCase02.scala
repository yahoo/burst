/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.parallel

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.maps.{HydraUnityCase12, HydraUnityCase14, HydraUnityCase20}
import org.burstsys.hydra.test.cases.unity.nested.HydraUnityCase13
import org.burstsys.hydra.test.cases.unity.refscalar._

object HydraUnityParaCase02 extends HydraUseCase(domain = 200, view = 200, schemaName = "unity") {

  val queries = Array(
    HydraUnityCase06,
    HydraUnityCase07,
    HydraUnityCase08,
    HydraUnityCase09,
    HydraUnityCase10,
    HydraUnityCase12,
    HydraUnityCase13,
    HydraUnityCase14,
    HydraUnityCase16,
    HydraUnityCase20
  )

  override val frameSource: String = queries.map(_.frameSource).mkString("\n")

  override def validate(implicit result: FabricResultGroup): Unit = queries.foreach(_.validate)


}
