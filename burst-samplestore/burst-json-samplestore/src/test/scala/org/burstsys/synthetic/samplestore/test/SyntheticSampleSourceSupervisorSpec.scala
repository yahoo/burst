/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.synthetic.samplestore.test

import org.burstsys.samplesource.SampleStoreTopology
import org.burstsys.samplestore.api.{BurstSampleStoreDataSource, BurstSampleStoreDomain, BurstSampleStoreView, SampleStoreDataLocus, SampleStoreGeneration}
import org.burstsys.samplestore.test.BaseSampleStoreTest
import org.burstsys.synthetic.samplestore.source.SyntheticSampleSourceSupervisor
import org.burstsys.synthetic.samplestore.{source, configuration => syntheticConfig}
import org.burstsys.{synthetic, vitals}
import org.burstsys.vitals.properties.VitalsPropertyMap
import org.scalatest.Inspectors.forAll
import org.scalatest.TryValues

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.{dynamics, postfixOps}
import scala.util.Try

class SyntheticSampleSourceSupervisorSpec extends BaseSampleStoreTest with TryValues {

  private val domain = BurstSampleStoreDomain(1)
  private val view = BurstSampleStoreView(1, "unity", "view foo {}")

  it should "have the proper name" in {
    val coordinator = SyntheticSampleSourceSupervisor()
    coordinator.name should equal(synthetic.samplestore.source.SyntheticSampleSourceName)
  }

  it should "generate loci using defaults" in {
    val emptyDataSource = BurstSampleStoreDataSource(domain, view)
    val generator = buildGenerator(emptyDataSource, Map.empty, "guid", 1).get
    generator.guid shouldBe "guid"
    generator.loci should have length 1
    forAll(generator.loci.toSeq)(locusValidator)
    if (syntheticConfig.defaultPersistentHashProperty.getOrThrow)
      generator.generationHash shouldBe source.InvariantHash
    else
      vitals.uid.isBurstUid(generator.generationHash) shouldBe true
  }

  it should "generate loci using properties in the datasource" in {
    val staticHash = Map(syntheticConfig.persistentHashProperty -> "true")
    val dynamichash = Map(syntheticConfig.persistentHashProperty -> "false")

    generationForView(view, 0).loci should have length 0
    generationForView(view, 10).loci should have length 10

    locusValidator(generationForView(view, 1).loci.head)

    generationForView(view.copy(viewProperties = staticHash), 1).generationHash shouldBe source.InvariantHash
    generationForView(view.copy(viewProperties = dynamichash), 1).generationHash should not be source.InvariantHash
  }

  private def generationForView(view: BurstSampleStoreView, lociCount: Int): SampleStoreGeneration =
    buildGenerator(BurstSampleStoreDataSource(domain, view), Map.empty, "guid", lociCount).get

   def buildGenerator(
                            dataSource: BurstSampleStoreDataSource,
                            properties: VitalsPropertyMap,
                            guid: String,
                            lociCount: Int = 0
                          ): Try[SampleStoreGeneration] = {
     val generator = SyntheticSampleSourceSupervisor()
     val topology = SampleStoreTopology(for (i <- 1 to lociCount) yield {
       val n = s"10.0.0.$i"
       SampleStoreDataLocus(n, n, n, i, Map.empty)
     })
     Await.ready(generator.getViewGenerator(guid, dataSource, topology, properties), 1 second).value.get
   }


  private def locusValidator(locus: SampleStoreDataLocus): Unit = {
    locus.hostName should startWith("10.0.0")
    locus.hostAddress should startWith("10.0.0")
  }

}
