/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.slice.metadata
import org.burstsys.fabric.data.model.slice.state.FabricDataCold
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.kryo._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suite}

class FabricKryoSpec extends FabricBaseSpec {

  "Fabric Transport" should "do a kryo serialization" in {

    val slices = Array(
      metadata.FabricSliceMetadata(datasource, 1, "host1"),
      metadata.FabricSliceMetadata(datasource, 3, "host2")
    )
    val inGeneration = FabricGeneration(datasource, slices)
    val outGeneration = FabricGeneration(datasource)

    try {
      val k = acquireKryo
      try {
        val output = new Output(50000)
        inGeneration.asInstanceOf[KryoSerializable].write(k, output)
        val encoded = output.toBytes

        val input = new Input(encoded)
        outGeneration.asInstanceOf[KryoSerializable].read(k, input)

      } finally releaseKryo(k)
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }

    outGeneration.datasource.domain.domainKey should equal(5)
    outGeneration.datasource.view.viewKey should equal(6)
    outGeneration.slices.length should equal(2)
    outGeneration.slices.head.state should equal(FabricDataCold)
    outGeneration.slices.last.state should equal(FabricDataCold)


  }
}
