/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.samplestore.model.{SampleStoreLocus, SampleStoreSlice, SampleStoreSliceContext}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.kryo.{acquireKryo, releaseKryo}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{BeforeAndAfterAll, Suite}

class SampleStoreSliceKryoSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with SampleStoreSpecLog {


  "SampleStore Slice" should "do a kryo serialization" in {

    val slice = SampleStoreSlice(
      "guid",
      1,
      1.toString,
      1,
      datasource,
      "motif",
      FabricWorkerNode(1, "localhost"),
      Array(
        SampleStoreLocus("suid", "local", "local", 1000,
          Map("key1" -> "value1")
        )
      )
    )
    val inSlice = slice
    val outSlice = new SampleStoreSliceContext()

    try {
      val k = acquireKryo
      try {
        val output = new Output(50000)
        inSlice.asInstanceOf[KryoSerializable].write(k, output)
        val encoded = output.toBytes

        val input = new Input(encoded)
        outSlice.asInstanceOf[KryoSerializable].read(k, input)

      } finally releaseKryo(k)
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }

    outSlice.guid should equal("guid")
    outSlice.sliceKey should equal(1)
    outSlice.generationHash should equal(1.toString)
    outSlice.slices should equal(1)
    outSlice.motifFilter should equal("motif")
    outSlice.worker.nodeId should equal(1)
    outSlice.datasource.domain.domainKey should equal(10)
    outSlice.datasource.view.viewKey should equal(9)
    outSlice.loci.length should equal(1)
    outSlice.loci.head.suid should equal("suid")
    outSlice.loci.head.partitionProperties should equal(Map("key1" -> "value1"))
  }
}
