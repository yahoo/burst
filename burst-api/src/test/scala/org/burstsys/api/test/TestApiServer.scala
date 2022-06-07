/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api.test

import org.burstsys.api.{ApiTwitterRequestFuture, BurstApiServer}
import org.burstsys.api.test.configuration._
import com.twitter.util.Future

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

private[test] final case
class TestApiServer(service: TestApiService) extends BurstApiServer with TestApi {


  override def testEndPoint(testMessage: String): Future[String] = {
    ApiTwitterRequestFuture {
      service.testEndPoint(testMessage) match {
        case Success(msg) =>
          msg
        case Failure(t) => throw t
      }
    }
  }


}
