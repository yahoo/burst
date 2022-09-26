/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.reporter

import org.burstsys.fabric
import org.burstsys.fabric.data.model.slice.region.FabricRegionReporter
import org.burstsys.fabric.data.worker.cache.FabricCacheReporter
import org.burstsys.fabric.execution.supervisor.wave.FabricWaveReporter
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKey
import org.burstsys.fabric.execution.model.gather.{FabricGather, FabricMerge}
import org.burstsys.fabric.execution.model.gather.metrics.FabricGatherMetrics
import org.burstsys.fabric.execution.model.result.{state, status}
import org.burstsys.fabric.execution.model.scanner.FabricScanner
import org.burstsys.fabric.execution.worker.FabricEngineReporter
import org.burstsys.fabric.metadata.model.FabricMetadataReporter
import org.burstsys.fabric.net.FabricNetReporter
import org.burstsys.fabric.topology.supervisor.FabricTopologyReporter
import org.burstsys.vitals.reporter
import org.scalatest.Ignore

import scala.concurrent.duration._
import scala.language.postfixOps

@Ignore
class FabricReporterSpec extends FabricAbstractSpec {

  val gather: FabricGather = new FabricGather {
    override def groupKey: FabricGroupKey = ???

    override def gatherMetrics: FabricGatherMetrics = ???


    override def scanner: FabricScanner = ???

    override def initialize(scanner: FabricScanner): this.type = ???

    override def resultMessage: String = ???

    override def regionMerge(merge: FabricMerge): Unit = ???

    override def sliceMerge(merge: FabricMerge): Unit = ???

    override def sliceFinalize(): Unit = ???

    override def waveMerge(merge: FabricMerge): Unit = ???

    override def waveFinalize(): Unit = ???
  }


  it should "test scatter reporter" in {
    reporter.startReporterSystem(samplePeriod = 1 second, reportPeriod = 1 second, waitPeriod = 1 second)
    FabricEngineReporter.successfulScan(10, gather)

    FabricEngineReporter.failedScan()

    FabricCacheReporter.onSnapColdLoad(mockSnap, 10, 10)

    FabricNetReporter.recordPing(10)

    FabricRegionReporter.countParcelWrite(10)

    FabricTopologyReporter.onTopologyWorkerGain(null)

    FabricWaveReporter.successfulAnalysis(10, mockGather)

    FabricMetadataReporter.recordDomainLookup()

    Thread.sleep((10 seconds).toMillis)
  }


}
