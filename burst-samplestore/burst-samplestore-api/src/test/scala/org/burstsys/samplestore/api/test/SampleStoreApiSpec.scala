/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.test

import org.burstsys.samplestore.api._
import org.burstsys.vitals.net._
import org.burstsys.vitals.properties._
import org.burstsys.vitals.uid._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps

//@Ignore
class SampleStoreApiSpec extends SampleStoreMasterSpec {


  "Sample Store API" should "do a mock client/server interchange" in {

    storeServiceServer talksTo new SampleStoreApiListener {
      override
      def getViewGenerator(guid: String, dataSource: BurstSampleStoreDataSource): Future[SampleStoreGenerator] = {
        val promise = Promise[SampleStoreGenerator]
        promise.success(
          SampleStoreGenerator(
            guid,
            guid,
            Array(
              SampleStoreDataLocus(suid = newBurstUid, ipAddress = getLocalHostAddress, hostName = getLocalHostName, port = 0, partitionProperties = "some other partition paradata"),
              SampleStoreDataLocus(suid = newBurstUid, ipAddress = getLocalHostAddress, hostName = getLocalHostName, port = 1, partitionProperties = "yet some more partition paradata")
            ),
            dataSource.view.schemaName,
            Some(dataSource.view.viewMotif)
          )
        )
        promise.future
      }
    }

    storeServiceServer.start
    storeServiceClient.start

    val domain1Properties: String =
      s"""
         | domain1_k1=domain1_v1; domain1_k2 = domain1_v2;
         | domain1_k3=domain1_v3;
     """.stripMargin

    val viewProperties: String =
      s"""
         | view_k1=view_v1; view_k2 = view_v2;
         | view_k3=view_v3;
     """.stripMargin

    val viewMotif = "some motif string"

    val dataSource = BurstSampleStoreDataSource(
      domain = BurstSampleStoreDomain(
        domainKey = 1,
        domainProperties = domain1Properties
      ),
      view = BurstSampleStoreView(
        viewKey = 1,
        schemaName = "quo",
        viewMotif = viewMotif,
        storeProperties = mockStoreProperties,
        viewProperties = viewProperties
      )
    )


    val future = storeServiceClient.getViewGenerator("guid", dataSource)

    val r = Await.result(future, 10 seconds)
    r.loci.get.length should equal(2)
    r.loci.get.head.hostName should equal(getLocalHostName)
    r.loci.get.head.hostAddress should equal(getLocalHostAddress)
    r.motifFilter should equal(Some(viewMotif))

  }

}
