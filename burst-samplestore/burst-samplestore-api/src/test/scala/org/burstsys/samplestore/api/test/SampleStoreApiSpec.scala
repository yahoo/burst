/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.api.test

import org.burstsys.api.twitterFutureToScalaFuture
import org.burstsys.samplestore.api._
import org.burstsys.vitals.net._
import org.burstsys.vitals.properties._
import org.burstsys.vitals.uid._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

//@Ignore
class SampleStoreApiSpec extends SampleStoreMasterSpec {


  "Sample Store API" should "do a mock client/server interchange" in {

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
      BurstSampleStoreDomain(1, domain1Properties),
      BurstSampleStoreView(1, "quo", viewMotif, mockStoreProperties, viewProperties)
    )


    val future = twitterFutureToScalaFuture(storeServiceClient.getViewGenerator(newBurstUid, dataSource))

    val r = Await.result(future, 10 seconds)
    r.loci.get.length should equal(2)
    r.loci.get.head.hostName should equal(getLocalHostName)
    r.loci.get.head.hostAddress should equal(getLocalHostAddress)
    r.motifFilter should equal(Some(viewMotif))

  }

}
