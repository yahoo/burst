/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.model.slice.data.FabricSliceData
import org.burstsys.fabric.wave.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.wave.data.model.snap.{FabricSnap, FabricSnapState}
import org.burstsys.fabric.wave.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.wave.execution.model.gather.metrics.FabricGatherMetrics
import org.burstsys.fabric.wave.execution.model.gather.{FabricGather, FabricMerge}
import org.burstsys.fabric.wave.execution.model.scanner.FabricScanner
import org.burstsys.vitals.logging._
import org.burstsys.vitals.uid.VitalsUid
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Path

package object reporter {

  trait FabricAbstractSpec extends AnyFlatSpec with Matchers {

    VitalsLog.configureLogging("fabric", true)

  }

  val mockSnap: FabricSnap = new FabricSnap {
    override def guid: VitalsUid = ???

    override def lastAccessTime: Long = ???

    override def evictTtlMs: Long = ???

    override def flushTtlMs: Long = ???

    override def evictTtlExpired: Boolean = ???

    override def flushTtlExpired: Boolean = ???

    override def totalAccessCount: Long = ???

    override def recordAccess: FabricSnap = ???

    override def slice: FabricSlice = ???

    override def slice_=(slice: FabricSlice): Unit = ???

    override def metadata: FabricSliceMetadata = ???

    override def releaseSnapReadLock(): FabricSnap = ???

    override def releaseSnapWriteLock(): FabricSnap = ???

    override def data: FabricSliceData = ???

    override def state: FabricSnapState = ???

    override def state_=(s: FabricSnapState): Unit = ???

    override def persist: FabricSnap = ???

    override def snapFile: Path = ???

    override def failCount: Int = ???

    override def lastFail_=(t: Throwable): Unit = ???

    override def lastFail: Option[Throwable] = ???

    override def trySnapWriteLock: Boolean = ???

    override def trySnapReadLock: Boolean = ???

    override def waitState(ms: Long): Unit = ???

    override def resetLastFail(): Unit = ???

    override def eraseTtlMs: Long = ???

    override def eraseTtlExpired: Boolean = ???

    override def delete: FabricSnap = ???
  }

  val mockGather: FabricGather = new FabricGather {

    override def resultMessage: String = ???

    override def groupKey: FabricGroupKey = ???

    override def gatherMetrics: FabricGatherMetrics = ???

    override def scanner: FabricScanner = ???

    override def initialize(scanner: FabricScanner): this.type = ???

    override def regionMerge(merge: FabricMerge): Unit = ???

    override def sliceMerge(merge: FabricMerge): Unit = ???

    override def sliceFinalize(): Unit = ???

    override def waveMerge(merge: FabricMerge): Unit = ???

    override def waveFinalize(): Unit = ???
  }


}
