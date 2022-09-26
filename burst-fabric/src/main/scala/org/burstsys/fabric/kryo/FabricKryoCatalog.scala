/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.kryo

import org.burstsys.fabric.container.model.metrics.{FabricAssessment, FabricLastHourMetric, FabricMetricTuple}
import org.burstsys.fabric.data.model.generation._
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKeyContext
import org.burstsys.fabric.data.model.generation.metrics.FabricGenerationMetricsContext
import org.burstsys.fabric.data.model.mode.{FabricColdLoad, FabricHotLoad, FabricUnknownLoad, FabricWarmLoad}
import org.burstsys.fabric.data.model.ops._
import org.burstsys.fabric.data.model.slice.data._
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadataContext
import org.burstsys.fabric.data.model.slice.state._
import org.burstsys.fabric.data.model.snap._
import org.burstsys.fabric.execution.model.execute.group.FabricGroupKeyContext
import org.burstsys.fabric.execution.model.execute.invoke._
import org.burstsys.fabric.execution.model.execute.parameters._
import org.burstsys.fabric.execution.model.gather.control.FabricFaultGatherContext
import org.burstsys.fabric.execution.model.gather.data.{FabricEmptyGatherContext, MockDataGather}
import org.burstsys.fabric.execution.model.gather.metrics.FabricGatherMetricsContext
import org.burstsys.fabric.execution.model.metrics.FabricExecutionMetricsContext
import org.burstsys.fabric.execution.model.result.FabricExecuteResultContext
import org.burstsys.fabric.execution.model.result.state._
import org.burstsys.fabric.execution.model.result.status._
import org.burstsys.fabric.execution.model.wave.{FabricParticleContext, FabricWaveContext}
import org.burstsys.fabric.metadata.model.datasource.FabricDatasourceContext
import org.burstsys.fabric.metadata.model.domain.FabricDomainContext
import org.burstsys.fabric.metadata.model.view.FabricViewContext
import org.burstsys.fabric.topology.model.node.supervisor.{FabricSupervisorContext, FabricSupervisorNodeContext}
import org.burstsys.fabric.topology.model.node.worker._
import org.burstsys.vitals.kryo._

import java.util.concurrent.atomic.AtomicInteger

/**
 * Kryo Serialized Class Register
 * TODO: in our post spark world - many of these no longer need to be serialized
 */
class FabricKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(fabricCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key.synchronized {
      Array(

        /////////////////////////////////////////////////////////////////////////////////
        // load mode
        /////////////////////////////////////////////////////////////////////////////////

        (key.getAndIncrement, FabricUnknownLoad.getClass),
        (key.getAndIncrement, FabricColdLoad.getClass),
        (key.getAndIncrement, FabricWarmLoad.getClass),
        (key.getAndIncrement, FabricHotLoad.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // scan state
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, FabricScanRunning.getClass),
        (key.getAndIncrement, FabricExceptionStatus.getClass),
        (key.getAndIncrement, FabricSuccessStatus.getClass),
        (key.getAndIncrement, FabricInvalidStatus.getClass),
        (key.getAndIncrement, FabricNoDataStatus.getClass),
        (key.getAndIncrement, FabricStoreErrorStatus.getClass),
        (key.getAndIncrement, FabricNotReadyStatus.getClass),
        (key.getAndIncrement, FabricTimeoutStatus.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // result status
        /////////////////////////////////////////////////////////////////////////////////

        (key.getAndIncrement, FabricUnknownResultStatus.getClass),
        (key.getAndIncrement, FabricSuccessResultStatus.getClass),
        (key.getAndIncrement, FabricFaultResultStatus.getClass),
        (key.getAndIncrement, FabricInvalidResultStatus.getClass),
        (key.getAndIncrement, FabricTimeoutResultStatus.getClass),
        (key.getAndIncrement, FabricNotReadyResultStatus.getClass),
        (key.getAndIncrement, FabricNoDataResultStatus.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // slice status
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, FabricDataCold.getClass),
        (key.getAndIncrement, FabricDataWarm.getClass),
        (key.getAndIncrement, FabricDataNoData.getClass),
        (key.getAndIncrement, FabricDataFailed.getClass),
        (key.getAndIncrement, FabricDataMixed.getClass),
        (key.getAndIncrement, FabricDataHot.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // assessment
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricAssessment]),
        (key.getAndIncrement, classOf[FabricMetricTuple]),
        (key.getAndIncrement, classOf[Array[FabricMetricTuple]]),
        (key.getAndIncrement, classOf[FabricLastHourMetric]),

        /////////////////////////////////////////////////////////////////////////////////
        // topology
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricSupervisorContext]),
        (key.getAndIncrement, classOf[FabricWorkerNodeContext]),
        (key.getAndIncrement, classOf[FabricSupervisorNodeContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // worker state
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, FabricWorkerStateUnknown.getClass),
        (key.getAndIncrement, FabricWorkerStateLive.getClass),
        (key.getAndIncrement, FabricWorkerStateTardy.getClass),
        (key.getAndIncrement, FabricWorkerStateFlaky.getClass),
        (key.getAndIncrement, FabricWorkerStateDead.getClass),
        (key.getAndIncrement, FabricWorkerStateExiled.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // domain/view/datasource
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricDatasourceContext]),
        (key.getAndIncrement, classOf[FabricDomainContext]),
        (key.getAndIncrement, classOf[FabricViewContext]),
        (key.getAndIncrement, classOf[FabricGroupKeyContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // slice classes
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricParticleContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // cache (generation/slice/operation)
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricCacheOpParameter]),
        (key.getAndIncrement, classOf[FabricGenerationKeyContext]),
        (key.getAndIncrement, classOf[FabricGenerationContext]),
        (key.getAndIncrement, classOf[Array[FabricGenerationContext]]),
        (key.getAndIncrement, classOf[FabricGenerationMetricsContext]),

        (key.getAndIncrement, classOf[FabricSliceMetadataContext]),
        (key.getAndIncrement, classOf[Array[FabricSliceMetadataContext]]),
        (key.getAndIncrement, classOf[FabricSliceDataContext]),

        (key.getAndIncrement, FabricCacheSearch.getClass),
        (key.getAndIncrement, FabricCacheEvict.getClass),
        (key.getAndIncrement, FabricCacheFlush.getClass),

        (key.getAndIncrement, FabricCacheLT.getClass),
        (key.getAndIncrement, FabricCacheGT.getClass),
        (key.getAndIncrement, FabricCacheEQ.getClass),

        (key.getAndIncrement, FabricCacheByteCount.getClass),
        (key.getAndIncrement, FabricCacheItemCount.getClass),
        (key.getAndIncrement, FabricCacheSliceCount.getClass),
        (key.getAndIncrement, FabricCacheRegionCount.getClass),
        (key.getAndIncrement, FabricCacheColdLoadAt.getClass),
        (key.getAndIncrement, FabricCacheColdLoadTook.getClass),
        (key.getAndIncrement, FabricCacheWarmLoadAt.getClass),
        (key.getAndIncrement, FabricCacheWarmLoadTook.getClass),
        (key.getAndIncrement, FabricCacheWarmLoadCount.getClass),
        (key.getAndIncrement, FabricCacheSizeSkew.getClass),
        (key.getAndIncrement, FabricCacheTimeSkew.getClass),
        (key.getAndIncrement, FabricCacheItemSize.getClass),
        (key.getAndIncrement, FabricCacheItemVariation.getClass),
        (key.getAndIncrement, FabricCacheLoadInvalid.getClass),
        (key.getAndIncrement, FabricCacheEarliestLoadAt.getClass),
        (key.getAndIncrement, FabricCacheRejectedItemCount.getClass),
        (key.getAndIncrement, FabricCachePotentialItemCount.getClass),
        (key.getAndIncrement, FabricCacheSuggestedSampleRate.getClass),
        (key.getAndIncrement, FabricCacheSuggestedSliceCount.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // scatter/gather
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricWaveContext]),
        (key.getAndIncrement, classOf[FabricGatherMetricsContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // report level
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, FabricTacitLevel.getClass),
        (key.getAndIncrement, FabricInfoLevel.getClass),
        (key.getAndIncrement, FabricTraceLevel.getClass),
        (key.getAndIncrement, FabricDebugLevel.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // parameters
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, FabricScalarForm.getClass),
        (key.getAndIncrement, FabricVectorForm.getClass),
        (key.getAndIncrement, FabricMapForm.getClass),
        (key.getAndIncrement, classOf[FabricCallContext]),
        (key.getAndIncrement, classOf[FabricParameterTypeContext]),
        (key.getAndIncrement, classOf[FabricParameterValueContext]),
        (key.getAndIncrement, classOf[FabricSignatureContext[_]]),

        /////////////////////////////////////////////////////////////////////////////////
        // execution
        /////////////////////////////////////////////////////////////////////////////////

        (key.getAndIncrement, classOf[FabricGroupKeyContext]),
//        (key.getAndIncrement, classOf[FabricViewInfoContext]),
        (key.getAndIncrement, classOf[FabricExecuteResultContext]),
        (key.getAndIncrement, classOf[FabricExecutionMetricsContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // invocation
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricParameterizationContext]),
        (key.getAndIncrement, classOf[FabricInvocationContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // built in gathers
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricFaultGatherContext]),
        (key.getAndIncrement, classOf[FabricEmptyGatherContext]),

        /////////////////////////////////////////////////////////////////////////////////
        // snap
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement, classOf[FabricSnap]),
        (key.getAndIncrement, classOf[FabricSnapContext]),
        (key.getAndIncrement, HotSnap.getClass),
        (key.getAndIncrement, ColdSnap.getClass),
        (key.getAndIncrement, WarmSnap.getClass),
        (key.getAndIncrement, NoDataSnap.getClass),
        (key.getAndIncrement, FailedSnap.getClass),

        /////////////////////////////////////////////////////////////////////////////////
        // mock
        /////////////////////////////////////////////////////////////////////////////////
        (key.getAndIncrement(), classOf[MockDataGather])
      )
    }
}
