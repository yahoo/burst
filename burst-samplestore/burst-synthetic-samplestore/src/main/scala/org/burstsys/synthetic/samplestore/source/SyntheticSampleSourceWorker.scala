/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.source

import org.burstsys.brio.flurry.provider.unity.BurstUnitySyntheticDataProvider
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.{BrioPressInstance, BrioPressSource}
import org.burstsys.brio.provider.{BrioSyntheticDataProvider, SyntheticDataProvider}
import org.burstsys.nexus.stream.NexusStream
import org.burstsys.synthetic.samplestore.configuration._
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.properties._

import scala.language.postfixOps

case class SyntheticSampleSourceWorker() extends ScanningSampleSourceWorker[BrioPressInstance]() {

  def name: String = SyntheticSampleSourceName

  override def prepareStats(stream: NexusStream, props: VitalsExtendedPropertyMap): BatchStats = {
    val globalItemCount = props.getValueOrProperty(defaultItemCountProperty)
    val batchCount = Math.max(1, props.getValueOrProperty(defaultBatchCountProperty))
    val itemCount = globalItemCount / batchCount
    val maxItemSize = props.getValueOrProperty(defaultMaxItemSizeProperty)
    val maxLoadSize = props.getValueOrProperty(defaultMaxLoadSizeProperty)
    val workerCount = props.getValueOrProperty(defaultWorkersCountProperty)
    val streamMaxSize = Math.max(maxLoadSize / workerCount, 1e6.toInt)
    val bs = new BatchStats(itemCount, streamMaxSize, maxItemSize, batchCount)
    bs.expectedItemCount.set(globalItemCount)
    bs
  }

  private val unityBrio = BurstUnitySyntheticDataProvider()
  private case class SyntheticDP(provider: BrioSyntheticDataProvider, props: VitalsExtendedPropertyMap, stats: BatchStats) extends DataProvider {
    override def scanner(stream: NexusStream, props: VitalsExtendedPropertyMap): Iterator[BrioPressInstance] = {
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

  override protected def getProvider(stream: NexusStream, props: VitalsExtendedPropertyMap, stats: BatchStats): DataProvider = {
    val modelName = stream.get[String](syntheticDatasetProperty)
    val dataProvider = {
      val p = SyntheticDataProvider.providerNamed(modelName)
      if (p == null) {
        log error burstStdMsg(s"Data provider $modelName not found, substituting ${unityBrio.schemaName}")
        unityBrio
      } else {
        p
      }
    }
    SyntheticDP(dataProvider, props, stats)
  }

}
