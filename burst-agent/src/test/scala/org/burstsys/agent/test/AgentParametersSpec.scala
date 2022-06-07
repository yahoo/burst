/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.test

import org.burstsys.brio.types.BrioTypes
import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.execution.model.execute.parameters.{FabricCall, FabricMapForm, FabricParameterValue, FabricScalarForm, FabricVectorForm}
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.metadata.model.over
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.errors.VitalsException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class AgentParametersSpec extends AgentQuerySpecSupport {

  private val d0 = 33
  private val d1 = Array(1, 2, 3, 4)
  private val d2 = Map("k1" -> "v1")

  it should "execute with scalar, vector, and map parameters" in {

    val call = FabricCall(
      FabricParameterValue.scalar[Int]("p1", d0),
      FabricParameterValue.vector[Int]("p2", d1),
      FabricParameterValue.map[String, String]("p3", d2),
      FabricParameterValue.nullScalar[Double]("p4")
    )

    Await.result( agentClient.execute(source = "mock kjdhsg", over = over.FabricOver(), guid = "foo", Some(call)), 10 minutes)

  }

  override
  def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    TeslaRequestFuture {
      val parameters = call.getOrElse(throw VitalsException(s"no call")).parameters
      parameters.length should equal(4)
      parameters.find(_.name == "p1") match {
        case None => fail()
        case Some(p) =>
          p.form should equal(FabricScalarForm)
          p.valueType should equal(BrioTypes.BrioIntegerKey)
          p.asScalar[Int] should equal(d0)
      }
      parameters.find(_.name == "p2") match {
        case None => fail()
        case Some(p) =>
          p.form should equal(FabricVectorForm)
          p.valueType should equal(BrioTypes.BrioIntegerKey)
          p.asVector[Int] should equal(d1)
      }
      parameters.find(_.name == "p3") match {
        case None => fail()
        case Some(p) =>
          p.form should equal(FabricMapForm)
          p.valueType should equal(BrioTypes.BrioStringKey)
          p.keyType should equal(BrioTypes.BrioStringKey)
          p.asMap[String, String] should equal(d2)
      }

      parameters.find(_.name == "p4") match {
        case None => fail()
        case Some(p) =>
          p.form should equal(FabricScalarForm)
          p.valueType should equal(BrioTypes.BrioDoubleKey)
          p.isNull should equal(true)
      }

      FabricExecuteResult()
    }
  }

}
