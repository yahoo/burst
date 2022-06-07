/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.parallel

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.cases.unity.refscalar.{HydraUnityCase06, HydraUnityCase07}

object HydraUnityParaCase01 extends HydraUseCase(domain = 200, view = 200, schemaName = "unity") {

  override val executionCount = 1
  //  override val serializeTraversal = true
  //  override val traceTraversal = true

  val queries = Array(
    HydraUnityCase06,
    HydraUnityCase07
  )

  override val frameSource: String = queries.map(_.frameSource).mkString("\n")

  override def validate(implicit result: FabricResultGroup): Unit = queries.foreach(_.validate)


}
