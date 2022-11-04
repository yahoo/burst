/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.execution.model

import com.esotericsoftware.kryo.KryoSerializable
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import org.burstsys.brio.blob.BrioBlob
import org.burstsys.fabric.wave.data.model.snap.FabricSnap
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.gather.FabricGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGather
import org.burstsys.fabric.wave.execution.model.gather.control.FabricFaultGatherContext
import org.burstsys.fabric.wave.execution.model.scanner.FabricScanner
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.test.FabricWaveBaseSpec
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.kryo.acquireKryo
import org.burstsys.vitals.kryo.releaseKryo

class FabricWaveGatherSpec extends FabricWaveBaseSpec {

  private val scanner = new FabricScanner {
    override def group: FabricGroupKey = FabricGroupKey()
    override def datasource: FabricDatasource = FabricDatasource(0L, 0L, 0L)
    override def snap: FabricSnap = ???
    override def initialize(group: FabricGroupKey, datasource: FabricDatasource): this.type = this
    override def beforeAllScans(snap: FabricSnap): this.type = this
    override def afterAllScans(snap: FabricSnap): this.type = this
    override def apply(v1: BrioBlob): FabricGather = ???
  }

  "FabricFaultGather" should "return false from succeeded" in {
    val gather = FabricFaultGather(scanner, VitalsException("Oops"))
    gather.succeeded should equal(false)
  }

  it should "return false after kryo" in {
    val k = acquireKryo
    val toWrite = FabricFaultGather(scanner, VitalsException("Oops"))
    val toRead = FabricFaultGatherContext()
    try {
      toWrite.succeeded should equal(false)
      val output = new Output(50000)
      toWrite.asInstanceOf[KryoSerializable].write(k, output)
      val encoded = output.toBytes

      val input = new Input(encoded)
      toRead.asInstanceOf[KryoSerializable].read(k, input)
      toRead.succeeded should equal(false)
    } finally releaseKryo(k)
  }
}
