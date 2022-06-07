/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.samplestore

import org.burstsys.alloy
import org.burstsys.brio.flurry.provider.quo
import org.burstsys.fabric.execution.model.result.status.FabricSuccessResultStatus
import org.burstsys.fabric.metadata.model
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.system.test.support.BurstSystemTestSpecSupport
import org.burstsys.tesla
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._
import org.scalatest.Ignore

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@Ignore
class SampleStoreExecuteSpec extends BurstSystemTestSpecSupport {


  it should "execute eql from sample store" in {
    val view = masterContainer.catalog.findViewByMoniker("BurstMasterTestView4").get

    val eql: String =
      s"""
         |select count(user) from schema quo
         |""".stripMargin

    val over = model.over.FabricOver(domain.pk, view.pk)
    val future = masterContainer.agent.execute(eql, over, "parcel-eql-sample-store")
    val r = Await.result(future, 10 minutes)
    r.resultStatus should equal(FabricSuccessResultStatus)
    r.resultGroup.get.resultSets.size should equal(1)
    val rs = r.resultGroup.get.resultSets.get(0)
    rs should not be null
    rs.get.rowSet.length should equal(1)
  }

  final override
  def feedStream(stream: NexusStream): Unit = {
    TeslaWorkerFuture {
      try {
        log info burstStdMsg(s"started feeding stream ${stream.suid}")
        val start = System.nanoTime()
        writeParcelsToStream(stream)
        stream put TeslaEndMarkerParcel
        log info burstStdMsg(s"done feeding stream ${stream.suid} in ${prettyTimeFromNanos(System.nanoTime - start)}")
      } catch safely {
        case t: Throwable => log error burstStdMsg(t)
      }
    }
  }

  override
  def abortStream(_stream: NexusStream, status: TeslaParcelStatus): Unit = {
  }

  private final val buffersPerParcel = 5

  private
  def writeParcelsToStream(stream: NexusStream): (Int, Int) = {
    val pathName = stream.properties.getValueOrThrow[String](alloy.store.AlloyViewDataPathProperty)
    val path = quo.getCachedSequenceFile(pathName)
    var itemCount = 0
    var bufferTally = 0
    var byteCount = 0
    var inflatedParcel = tesla.parcel.factory.grabParcel(10e6.toInt)
    inflatedParcel.startWrites()

    def pushOut(): Unit = {
      bufferTally = 0
      stream put inflatedParcel
      inflatedParcel = tesla.parcel.factory.grabParcel(10e6.toInt)
      inflatedParcel.startWrites()
    }
    ???
  }

}
