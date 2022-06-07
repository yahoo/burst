/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import java.util.concurrent.TimeUnit

import org.burstsys.catalog
import org.burstsys.catalog.model.domain._
import org.burstsys.catalog.model.view._
import org.burstsys.torquemada.{Driver, Parameters}

import scala.collection.mutable
import scala.util.{Failure, Success}

class TorquemadaSpec extends TorquemadaHelper {

  "torcher" must "use query file and dataset primary key list with no duration" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("src/test/resources/primaryKeys.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop
    val stats = driver.jobSettings.statTable.tableStatistics

    assert(driver.runningTime > TimeUnit.MILLISECONDS.toNanos(10))
    assert(queryCount.get() == 3)
    assert(stats.size == 1)
    log info s"\n${driver.jobSettings.statTable.tableStatistics()}"
    val l = driver.jobSettings.statTable.tableStatisticsLabels
    log info l.mkString(", ")
    log info s"\n${driver.jobSettings.statTable.tableStatistics(labels = false)}"
  }

  it must "use query file and dataset moniker list with no duration" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("src/test/resources/moniker.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.MILLISECONDS.toNanos(10))
    assert(queryCount.get() == 3 * 3)
  }

  it must "use query file and project id list with no duration" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("src/test/resources/projectIds.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    // tag the canned domains with a "project_id" tag so they can be found
    catalogClient allDomains() match {
      case Success(domains) =>
        for ((d, n) <- domains.zipWithIndex) {
          val labels = mutable.Map[String, String]()
          labels ++= d.labels.getOrElse(Map())
          labels("project_id") = (101 + n).toString
          // "burst.view.project.id"
          catalogClient.ensureDomain(d.copy(labels = Some(labels))) match {
            case Success(_) =>
            case Failure(e) =>
              fail(s"unable to add label to domain ${d.pk}- ${d.moniker}", e)
          }
        }
      case Failure(e) =>
        fail(s"unable to retrieve domains", e)
    }

    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.MILLISECONDS.toNanos(10))
    assert(queryCount.get() == 4 * 3)
  }

  it must "create temporary domains and views for project ids" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("src/test/resources/projectIds.json"))
    if (params.isEmpty) fail("parser failed")

    val driver = Driver(params.get, agentClient, catalogClient).addListener((l, m) => log.log(l, m)).start
    driver.run

    // test the temporary domains are there
    catalogClient.searchDomainsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        assert(views.length == 4)
      case Failure(_) =>
        fail("no temporary views found")
    }
    // test the temporary views are there
    catalogClient.searchViewsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        assert(views.length == 4)
      case Failure(_) =>
        fail("no temporary views found")
    }
    driver.stop
    // test the temporary domains are gone
    catalogClient.searchDomainsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        assert(views.length == 0)
      case Failure(_) =>
        fail("no temporary views found")
    }
    // test the temporary views are gone
    catalogClient.searchViewsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        assert(views.length == 0)
      case Failure(_) =>
        fail("no temporary views found")
    }

    assert(driver.runningTime > TimeUnit.MILLISECONDS.toNanos(10))
    assert(queryCount.get() == 4 * 3)
  }

  it must "create temporary views and delete them" in {
    resetTestCounters()

    val params = Parameters.parseArguments(Array("src/test/resources/primaryKeys.json"))
    if (params.isEmpty) fail("parser failed")

    val driver = Driver(params.get, agentClient, catalogClient).addListener((l, m) => log.log(l, m)).start
    driver.run
    // test the temporary views are there
    catalogClient.searchViewsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        assert(views.length == 1)
      case Failure(_) =>
        fail("no temporary views found")
    }

    driver.stop
    // test the temporary views are gone
    catalogClient.searchViewsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        assert(views.length == 0)
      case Failure(_) =>
        fail("no temporary views found")
    }

    assert(driver.runningTime >= TimeUnit.MILLISECONDS.toNanos(1))
    assert(queryCount.get() == 3)
  }

  it must "use fuse views" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-d", "3seconds", "src/test/resources/projectIds-2-fuse.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    // tag the canned domains with a "project_id" tag so they can be found
    // and add a fake fuse view
    val fuseViews: Array[Long] = catalogClient.allDomains() match {
      case Success(domains) =>
        val storeProperties = Map(
          "burst.store.name" -> "fuse"
        )
        for ((d, n) <- domains.zipWithIndex) yield {
          val labels = mutable.Map[String, String]()
          labels ++= d.labels.getOrElse(Map())
          labels("project_id") = (101 + n).toString
          // "burst.view.project.id"
          catalogClient.ensureDomain(d.copy(labels = Some(labels))) match {
            case Success(_) =>
            case Failure(e) =>
              fail(s"unable to add label to domain ${d.pk}- ${d.moniker}", e)
          }
          val viewLabels = Some(Map(catalog.torcherDataLabel -> "true"))
          val view = s"Torcher-${System.nanoTime()}"
          val v = CatalogView(0, view, d.pk, "unity", storeProperties = storeProperties, labels = viewLabels, udk = Some(view))
          catalogClient.ensureView(v) match {
            case Success(vPk) => vPk.toLong
            case Failure(_) => fail(s"failed to create view for domain ${d.pk}")
          }
        }
      case Failure(e) =>
        fail(s"unable to retrieve domains", e)
    }
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(3))
    assert(queryCount.get() > 0)
    assert(queryCount.get() == 3 * flushCount.get)
    // clean up the fake fuse views
    for (v <- fuseViews) {
      // make sure the fuse view wasn't deleted
      catalogClient findViewByPk v match {
        case Success(_) =>
        case Failure(_) => fail(s"failed to find original fuse view $v")
      }

      // now we clean up
      catalogClient deleteView v match {
        case Success(vPk) => vPk
        case Failure(_) =>
          fail(s"failed to delete view $v")
          null
      }

    }
  }

  it must "use tagged views and duration with explicit cache flush" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-d", "10seconds", "src/test/resources/tagged-withflush.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(10))
    assert(queryCount.get() > 0)
    assert(flushCount.get() > 0)
    assert(queryCount.get() == 3 * flushCount.get)
  }

  it must "use tagged views and duration with generation update" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("src/test/resources/tagged-withgeneration.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient).addListener((l, m) => log.log(l, m)).start
    driver.run

    // one view per domain
    val expectedViewCount = catalogClient.searchDomainsByLabel(catalog.cannedDataLabel) match {
      case Success(views) => views.length
      case Failure(_) => -1
    }

    // test the temporary views are there
    catalogClient.searchViewsByLabel(catalog.torcherDataLabel) match {
      case Success(views) =>
        for (v <- views) {
          assert(v.createTimestamp.isDefined && v.generationClock > v.createTimestamp.get)
        }
        assert(views.length == expectedViewCount)
      case Failure(_) =>
        fail("no temporary views found")
    }

    driver.stop

    assert(queryCount.get() > 0)
    assert(flushCount.get() == 0)
  }

  it must "not flush when instructed" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("-m", "localhost", "-d", "5seconds", "src/test/resources/tagged.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(driver.runningTime > TimeUnit.SECONDS.toNanos(5))
    assert(queryCount.get() > 0)
    assert(flushCount.get() == 0)
  }

  it must "limit datasets when instructed" in {
    resetTestCounters()
    val params = Parameters.parseArguments(Array("src/test/resources/tagged-withlimit.json"))
    if (params.isEmpty) fail("parser failed")

    implicit val (catalogClient, agentClient) = Driver.openClients(params.get)
    val driver = Driver(params.get, agentClient, catalogClient)
    driver.addListener((l, m) => log.log(l, m)).start.run.stop

    assert(queryCount.get() == 1 * 3)
    assert(flushCount.get() == 0)
  }
}
