/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.samplestore

import org.burstsys.fabric.wave.execution.model.result.status.FabricSuccessResultStatus
import org.burstsys.fabric.wave.metadata.model
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.system.test.support.BurstSystemTestSpecSupport
import org.burstsys.tesla.parcel._
import org.burstsys.tesla.thread.worker.TeslaWorkerFuture
import org.burstsys.vitals.errors._
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._
import org.scalatest.Ignore

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@Ignore
class SampleStoreExecuteSpecServer extends BurstSystemTestSpecSupport {


  it should "execute eql from sample store" in {
    val view = supervisorContainer.catalog.findViewByMoniker("BurstSupervisorTestView4").get

    val eql: String =
      s"""
         |select count(user) from schema quo
         |""".stripMargin

    val over = model.over.FabricOver(domain.pk, view.pk)
    val future = supervisorContainer.agent.execute(eql, over, "parcel-eql-sample-store")
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
        stream.complete(???, ???, ???, ???)
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
    ???
  }

}
