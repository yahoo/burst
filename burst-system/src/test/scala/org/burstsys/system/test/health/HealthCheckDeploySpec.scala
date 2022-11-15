/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.system.test.health

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.burstsys.system.test.support.BurstSystemTestSpecSupport
import org.burstsys.vitals.configuration
import org.burstsys.vitals.healthcheck._
import org.joda.time.DateTime
import org.joda.time.Period

import scala.concurrent.duration._
import scala.language.postfixOps

class HealthCheckDeploySpec extends BurstSystemTestSpecSupport {

  private var healthCheckPeriod: Long = _

  override protected def beforeAll(): Unit = {
    healthCheckPeriod = configuration.burstVitalsHealthCheckPeriodMsProperty.get
    // make the container update its healthcheck cache every 100 ms
    configuration.burstVitalsHealthCheckPeriodMsProperty.set(100)
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    configuration.burstVitalsHealthCheckPeriodMsProperty.set(healthCheckPeriod)
    super.afterAll()
  }

  it should "see if supervisor responds to health check" in {
    val port = this.supervisorContainer.health.healthCheckPort
    val paths = org.burstsys.vitals.configuration.burstVitalsHealthCheckPathsProperty.get.split(",")
    val checkPath = s"http://localhost:$port${paths.last}"

    val client = HttpClients.createDefault()
    val get = new HttpGet(checkPath)
    val resp = client.execute(get)
    try assert(resp.getStatusLine.getStatusCode == VitalsHealthHealthy.statusCode)
    finally {
      resp.close()
      client.close()
    }
  }

  it should "see if worker responds to health check" in {
    val port = this.workerContainer.health.healthCheckPort
    val paths = org.burstsys.vitals.configuration.burstVitalsHealthCheckPathsProperty.get.split(",")
    val checkPath = s"http://localhost:$port${paths.last}"

    val client = HttpClients.createDefault()
    val get = new HttpGet(checkPath)
    val resp = client.execute(get)
    try assert(resp.getStatusLine.getStatusCode == VitalsHealthHealthy.statusCode)
    finally {
      resp.close()
      client.close()
    }
  }

  it should "see if supervisor expires health check after a set duration" in {
    val port = this.supervisorContainer.health.healthCheckPort
    val paths = org.burstsys.vitals.configuration.burstVitalsHealthCheckPathsProperty.get.split(",")
    val checkPath = s"http://localhost:$port${paths.last}"

    // put a lifetime on the process
    val life = VitalsHealthLifetimeComponent(null, Period.parse("PT2s"))
    this.supervisorContainer.health.registerComponent(life)
    val client = HttpClients.createDefault()
    val get = new HttpGet(checkPath)
    try {
      Thread.sleep((1.5 seconds).toMillis)
      val respOne = client.execute(get)
      try assert(respOne.getStatusLine.getStatusCode == VitalsHealthHealthy.statusCode)
      finally respOne.close()

      // wait for timeout and the status to be refreshed
      Thread.sleep((0.75 seconds).toMillis)
      val respTwo = client.execute(get)
      try assert(respTwo.getStatusLine.getStatusCode == VitalsHealthUnhealthy.statusCode)
      finally respTwo.close()
      this.supervisorContainer.health.deregisterComponent(life)

      Thread.sleep((1 second).toMillis)
      val respThree = client.execute(get)
      try assert(respOne.getStatusLine.getStatusCode == VitalsHealthHealthy.statusCode)
      finally respThree.close()

    } finally {
      this.supervisorContainer.health.deregisterComponent(life)
      client.close()
    }
  }

  it should "see if supervisor expires health check at a set time" in {
    val port = this.supervisorContainer.health.healthCheckPort
    val paths = org.burstsys.vitals.configuration.burstVitalsHealthCheckPathsProperty.get.split(",")
    val checkPath = s"http://localhost:$port${paths.last}"

    val client = HttpClients.createDefault()
    val get = new HttpGet(checkPath)
    val respOne = client.execute(get)
    try assert(respOne.getStatusLine.getStatusCode == VitalsHealthHealthy.statusCode)
    finally respOne.close()

    // put a lifetime on the process with a cutoff date
    val cutoff = DateTime.now.toLocalTime.plusSeconds(2)

    val life = VitalsHealthLifetimeComponent(cutoff, Period.parse("PT1M"))
    this.supervisorContainer.health.registerComponent(life)
    try {
      // wait for timeout and the status to be refreshed
      Thread.sleep((2.5 seconds).toMillis)
      val respTwo = client.execute(get)
      try assert(respTwo.getStatusLine.getStatusCode == VitalsHealthUnhealthy.statusCode)
      finally respTwo.close()

    } finally {
      client.close()
      this.supervisorContainer.health.deregisterComponent(life)
    }
  }

}
