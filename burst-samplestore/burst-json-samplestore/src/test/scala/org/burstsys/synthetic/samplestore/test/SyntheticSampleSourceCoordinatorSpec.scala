/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.test

import org.burstsys.samplestore.api.BurstSampleStoreDataSource
import org.burstsys.samplestore.api.BurstSampleStoreDomain
import org.burstsys.samplestore.api.BurstSampleStoreView
import org.burstsys.samplestore.api.SampleStoreDataLocus
import org.burstsys.samplestore.api.SampleStoreGeneration
import org.burstsys.samplestore.test.BaseSampleStoreTest
import org.burstsys.synthetic
import org.burstsys.synthetic.samplestore.service
import org.burstsys.synthetic.samplestore.service.SyntheticSampleSourceCoordinator
import org.burstsys.synthetic.samplestore.{configuration => syntheticConfig}
import org.burstsys.vitals
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.scalatest.Inspectors.forAll
import org.scalatest.TryValues

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.dynamics
import scala.language.postfixOps
import scala.util.Try

class SyntheticSampleSourceCoordinatorSpec extends BaseSampleStoreTest with TryValues {

  private val domain = BurstSampleStoreDomain(1)
  private val view = BurstSampleStoreView(1, "unity", "view foo {}")

  it should "have the proper name" in {
    val coordinator = SyntheticSampleSourceCoordinator()
    coordinator.name should equal(synthetic.samplestore.service.SynteticSampleSourceName)
  }

  it should "generate loci using defaults" in {
    val emptyDataSource = BurstSampleStoreDataSource(domain, view)
    val generator = buildGenerator(emptyDataSource, Map.empty, "guid").get
    generator.guid shouldBe "guid"
    generator.loci should have length syntheticConfig.defaultLociCountProperty.getOrThrow
    forAll(generator.loci.toSeq)(locusValidator()(_))
    if (syntheticConfig.defaultPersistentHashProperty.getOrThrow)
      generator.generationHash shouldBe service.InvariantHash
    else
      vitals.uid.isBurstUid(generator.generationHash) shouldBe true
  }

  it should "generate loci using properties in the datasource" in {
    val noLoci = Map(syntheticConfig.lociCountProperty -> "0")
    val tenLoci = Map(syntheticConfig.lociCountProperty -> "10")
    val asLocalHost = Map(syntheticConfig.lociCountProperty -> "1", syntheticConfig.useLocalHostProperty -> "true")
    val trueHostname = Map(syntheticConfig.lociCountProperty -> "1", syntheticConfig.useLocalHostProperty -> "false")
    val staticHash = Map(syntheticConfig.persistentHashProperty -> "true")
    val dynamichash = Map(syntheticConfig.persistentHashProperty -> "false")

    generationForView(view.copy(viewProperties = noLoci)).loci should have length 0
    generationForView(view.copy(viewProperties = tenLoci)).loci should have length 10

    locusValidator(localHost = true)(generationForView(view.copy(viewProperties = asLocalHost)).loci.head)
    val badGeneration = buildGenerator(BurstSampleStoreDataSource(domain, view.copy(viewProperties = trueHostname)), Map.empty, "guid")
    badGeneration.failure.exception should have message "Only localhost mode is currently supported"

    generationForView(view.copy(viewProperties = staticHash)).generationHash shouldBe service.InvariantHash
    generationForView(view.copy(viewProperties = dynamichash)).generationHash should not be service.InvariantHash
  }

  private def generationForView(view: BurstSampleStoreView): SampleStoreGeneration =
    buildGenerator(BurstSampleStoreDataSource(domain, view), Map.empty, "guid").get

  private def buildGenerator(
                            dataSource: BurstSampleStoreDataSource,
                            properties: VitalsPropertyMap,
                            guid: String,
                          ): Try[SampleStoreGeneration] =
    Await.ready(SyntheticSampleSourceCoordinator().getViewGenerator(guid, dataSource, properties), 1 second).value.get

  private def locusValidator(localHost: Boolean = syntheticConfig.defaultUseLocalHostProperty.getOrThrow)(locus: SampleStoreDataLocus): Unit = {
    locus.hostAddress shouldBe (if (localHost) "127.0.0.1" else vitals.net.getLocalHostAddress)
    locus.hostName shouldBe (if (localHost) "localhost" else vitals.net.getLocalHostName)
  }

}
