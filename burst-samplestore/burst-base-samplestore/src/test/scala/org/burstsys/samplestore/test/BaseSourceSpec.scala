/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test

import org.burstsys.api.twitterFutureToScalaFuture
import org.burstsys.samplesource.SampleStoreTopology
import org.burstsys.samplesource.service.MetadataParameters
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, BurstSampleStoreDomain, BurstSampleStoreView, SampleStoreDataLocus, SampleStoreSourceNameProperty}
import org.burstsys.samplestore.test.source.{InvariantHash, TestSampleSourceName, TestSourceListener}
import org.burstsys.vitals.properties.VitalsPropertyMap

import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class BaseSourceSpec extends BaseSupervisorWorkerBaseSpec
  with TestSourceListener {


  override protected def wantsContainers = true

  override protected def workerCount = 3

  override protected
  def beforeAll(): Unit = {
    super.beforeAll()
    source.testListener = Some(this)
  }

  def latch(count: Int = workerCount): CountDownLatch = new CountDownLatch(count)

  private val workerGain: CountDownLatch = latch()
  private val workerLoss: CountDownLatch = latch()
  private val viewGen: CountDownLatch = latch(1)
  private val metaGet: CountDownLatch = latch(1)
  private val metaPut: CountDownLatch = latch()

  it should "initiate a source" in {
    // check for startup
    workerGain.await(30, TimeUnit.SECONDS) shouldEqual true

    val domainProps = Map("domain"-> "test")
    val viewStoreProps = Map( SampleStoreSourceNameProperty -> TestSampleSourceName, "viewStore"-> "test")
    val viewViewProps = Map("viewView"-> "test")
    val datasource = BurstSampleStoreDataSource(
      BurstSampleStoreDomain(1, domainProps),
      BurstSampleStoreView(1, "", "", viewStoreProps, viewViewProps)
    )
    val r = Await.result(storeServiceClient.getViewGenerator("test", datasource), 15.seconds)
    r.generationHash should equal(InvariantHash)
    viewGen.await(15, TimeUnit.SECONDS) shouldEqual true


    Await.result(supervisorContainer.updateMetadata(TestSampleSourceName), 15.seconds)
    metaPut.await(15, TimeUnit.SECONDS) shouldEqual true
    metaGet.await(15, TimeUnit.SECONDS) shouldEqual true

    // shut them down
    workerContainers.foreach(_.stop)
    Thread.sleep(100)
    workerLoss.await(15, TimeUnit.SECONDS) shouldEqual true

  }

  override def onGetViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource, topology: SampleStoreTopology, listenerProperties: VitalsPropertyMap): Unit = {
    dataSource.view.storeProperties.keys should contain ("viewStore")
    topology.loci.size should equal(workerCount)
    viewGen.countDown()
  }

  override def onPutBroadcastVars(metadata: MetadataParameters): Unit = {
    metadata.keys should contain ("akey")
    metadata.keys should contain ("bkey")
    metadata.keys should contain ("skey")
    metadata("skey").asInstanceOf[String] should equal("true")
    metadata("bkey").asInstanceOf[Boolean] should equal(true)
    val m = metadata("akey").asInstanceOf[java.util.HashMap[java.lang.Long, java.lang.Long]]
    m.get(1.toLong) should equal(2)
    metaPut.countDown()
  }

  override def onGetBroadcastVars(): MetadataParameters = {
    metaGet.countDown()
    scala.collection.Map(
      "skey"-> "true".asInstanceOf[Serializable],
      "bkey" -> true.asInstanceOf[Serializable],
      "akey" -> {val m = new java.util.HashMap[java.lang.Long, java.lang.Long](); m.put(1,2); m}.asInstanceOf[Serializable]
    ).toMap
  }

  override def onSampleStoreDataLocusRemoved(locus: SampleStoreDataLocus): Unit = {
    workerLoss.countDown()
  }

  override def onSampleStoreDataLocusAdded(locus: SampleStoreDataLocus): Unit = {
    workerGain.countDown()
  }
}
