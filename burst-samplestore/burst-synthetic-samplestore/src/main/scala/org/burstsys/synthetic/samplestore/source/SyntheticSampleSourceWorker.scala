/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.brio.provider.{BrioSyntheticDataProvider, SyntheticDataProvider}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.samplesource
import org.burstsys.samplesource.pipeline.PressPipeline
import org.burstsys.samplesource.service.scanning.{BatchControl, FeedControl, ScanningSampleSourceWorker}
import org.burstsys.synthetic.samplestore.configuration._
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.properties._

import scala.language.postfixOps

/**
 * A sample source worker that generates synthetic data.
 */
case class SyntheticSampleSourceWorker() extends ScanningSampleSourceWorker[BrioPressInstance, FeedControl, BatchControl]() {


  def name: String = SyntheticSampleSourceName

  def prepareFeedControl(stream: NexusStream): FeedControl = {
    val props: VitalsExtendedPropertyMap = stream.properties.extend
    val globalItemCount = props.getValueOrProperty(defaultItemCountProperty)
    val timeout = props.getValueOrProperty(defaultPressTimeoutProperty)
    new FeedControl(timeout, globalItemCount)
  }

  def prepareBatchControls(feedControl: FeedControl, stream: NexusStream): Iterable[BatchControl] = {
    val props: VitalsExtendedPropertyMap = stream.properties.extend
    val globalItemCount = props.getValueOrProperty(defaultItemCountProperty)
    val batchCount = Math.max(1, props.getValueOrProperty(defaultBatchCountProperty))
    val itemCount = globalItemCount / batchCount
    val maxItemSize = props.getValueOrProperty(defaultMaxItemSizeProperty)
    val maxLoadSize = props.getValueOrProperty(defaultMaxLoadSizeProperty)
    val workerCount = props.getValueOrProperty(defaultWorkersCountProperty)
    val streamMaxSize = Math.max(maxLoadSize / workerCount, 1e6.toInt)
    (1 to batchCount).map { i =>
      new BatchControl(stream, feedControl, i, itemCount, streamMaxSize, maxItemSize)
    }
  }

  override def finalizeBatch(control: BatchControl): BatchControl = control

  def finalizeBatchResults(feedControl: FeedControl, results: Iterable[BatchResult]): Unit = {
    log info burstStdMsg(s"synthetic samplestore batch processing complete")
    if (log.isDebugEnabled())
      results.foreach(r => log debug burstStdMsg(s"batch ${r.control.id} (itemCount=${r.itemCount}, skipped=${r.skipped}"))
  }

  private val unityBrio = BurstUnitySyntheticDataProvider()
  private case class SyntheticDP(provider: BrioSyntheticDataProvider, stats: BatchControl) extends DataProvider {
    override def scanner(stream: NexusStream): Iterator[BrioPressInstance] = {
      provider.data(stats.itemCount, stream.properties)
    }

    override def pressInstance(item: BrioPressInstance): BrioPressInstance = {
      item
    }

    override def pressSource(item: BrioPressInstance): BrioPressSource = {
      provider.pressSource(item)
    }

    override def schema: BrioSchema = {
      BrioSchema(provider.schemaName)
    }
  }

  override protected def getProvider(control: BatchControl): DataProvider = {
    val modelName = control.stream.get[String](syntheticDatasetProperty)
    val dataProvider = {
      val p = SyntheticDataProvider.providerNamed(modelName)
      if (p == null) {
        log error burstStdMsg(s"Data provider $modelName not found, substituting ${unityBrio.schemaName}")
        unityBrio
      } else {
        p
      }
    }
    SyntheticDP(dataProvider, control)
  }
}
