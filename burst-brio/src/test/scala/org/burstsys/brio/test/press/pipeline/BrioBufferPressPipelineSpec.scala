/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.press.pipeline

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.pipeline
import org.burstsys.brio.test.BrioAbstractSpec
import org.burstsys.brio.test.press.BrioMockPressSource
import org.burstsys.tesla.buffer.factory._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

//@Ignore
class BrioBufferPressPipelineSpec extends BrioAbstractSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  it should "start buffer press in parallel" in {

    val presserSchema = BrioSchema("presser")
    val futures = for (i <- 0 until 1000) yield
      pipeline.pressToFuture("fake-guid", BrioMockPressSource(), presserSchema,
        1, 10000000)

    val results = Await.result(Future.sequence(futures), 10 minutes)
    results.foreach {
      result =>
        releaseBuffer(result)
    }

  }

}
