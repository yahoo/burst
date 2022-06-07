/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import org.burstsys.catalog
import org.burstsys.samplestore.api.SampleStoreSourceNameProperty
import org.burstsys.samplestore.api.SampleStoreSourceVersionProperty
import org.burstsys.torquemada.Driver
import org.burstsys.torquemada.Parameters

import scala.util.Failure
import scala.util.Success

class TorquemadaDefaultOverridesSpec extends TorquemadaHelper {

  it must "create temporary views with standard sample store properties" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("src/test/resources/projectIds.json"))
    if (params.isEmpty) fail("parser failed")

    val driver = Driver(params.get, agentClient, catalogClient).addListener((l, m) => log.log(l, m)).start
    driver.run

    // test the temporary views are there
    catalogClient.searchViewsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        // check that the created views have the overriden properties
        for (v <- views) {
          assert(v.labels.get(catalog.torcherDataLabel) == "true")
          assert(v.storeProperties.get(SampleStoreSourceNameProperty).contains("AppEventsBrio"))
          assert(v.storeProperties.get(SampleStoreSourceVersionProperty).contains("0.0"))
        }
      case Failure(e) =>
        fail("no temporary views found")
    }
    driver.stop

    assert(queryCount.get() == 4 * 3)
    //    TODO assert(driver.runningTime < MILLISECONDS.toNanos(30) && driver.runningTime > MILLISECONDS.toNanos(1))
  }

  it must "create temporary views with overriden properties" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("src/test/resources/projectIds-override.json"))
    if (params.isEmpty) fail("parser failed")

    val driver = Driver(params.get, agentClient, catalogClient).addListener((l, m) => log.log(l, m)).start
    driver.run

    // test the temporary views are there
    catalogClient.searchViewsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        // check that the created views have the overriden properties
        for (v <- views) {
          assert(v.labels.get(catalog.torcherDataLabel) == "maybe")
          assert(v.storeProperties.get(SampleStoreSourceNameProperty).contains("AppEventsMockBrio"))
          assert(v.storeProperties.get(SampleStoreSourceVersionProperty).contains("0.1"))
        }
      case Failure(e) =>
        fail("no temporary views found")
    }
    driver.stop

    //   TODO  assert(driver.runningTime > MILLISECONDS.toNanos(10))
    assert(queryCount.get() == 4 * 3)
  }

}
