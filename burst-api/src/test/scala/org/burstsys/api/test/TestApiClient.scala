/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api.test

import org.burstsys.api.BurstApiClient
import org.burstsys.vitals.errors._
import com.twitter.util.Future

import scala.language.{implicitConversions, postfixOps}
import org.burstsys.vitals.logging._


final case
class TestApiClient(service: TestApiService) extends BurstApiClient[BurstTestApiService.MethodPerEndpoint] with TestApi {

  override def testEndPoint(testMessage: String): Future[String] = {
    try {
      ensureRunning
      thriftClient.testEndPoint(testMessage)
    } catch safely {
      case t: Throwable =>
        log error burstStdMsg(t)
        throw t
    }
  }

}
