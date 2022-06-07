/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.data

import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.execution.model.execute.parameters.{FabricCall, FabricParameterType, FabricParameterValue, FabricSignature}
import org.burstsys.fabric.test.FabricMasterWorkerBaseSpec
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.kryo.{acquireKryo, releaseKryo}
import org.scalatest.Ignore

import scala.language.postfixOps

class FabricParameterSpec extends FabricMasterWorkerBaseSpec {

  val p1Name = "p1"
  val p2Name = "p2"
  val p3Name = "p3"

  //  define  signature
  private val pt1 = FabricParameterType.scalar[Int](p1Name)
  private val pt2 = FabricParameterType.vector[Int](p2Name)
  private val pt3 = FabricParameterType.map[String, String](p3Name)

  val s1 = FabricSignature(pt1, pt2, pt3)

  // create  call
  private val pv1 = FabricParameterValue.scalar[Int](p1Name, 45)
  private val pv2 = FabricParameterValue.vector[Int](p2Name, Array(4, 2))
  private val pv3 = FabricParameterValue.map[String, String](p3Name, Map("k1" -> "v1"))

  val c1 = FabricCall(pv1, pv2, pv3)

  it should "validate call against signature" in {

    // ensure that call matches signature
    assert(s1.validate(c1))

  }

  it should "kryo serialize signature" in {

    try {
      val k = acquireKryo
      try {
        val output = new Output(1e6.toInt)
        s1.write(k, output)
        val encoded = output.toBytes

        val input = new Input(encoded)
        val tmp = FabricSignature()
        tmp.read(k, input)

      } finally releaseKryo(k)
    } catch safely {
      case t: Throwable => throw VitalsException(t)

    }
  }

  it should "kryo serialize call" in {

    try {
      val k = acquireKryo
      try {
        val output = new Output(1e6.toInt)
        c1.write(k, output)
        val encoded = output.toBytes

        val input = new Input(encoded)
        val tmp = FabricCall()
        tmp.read(k, input)

      } finally releaseKryo(k)
    } catch safely {
      case t: Throwable => throw VitalsException(t)

    }
  }


}
