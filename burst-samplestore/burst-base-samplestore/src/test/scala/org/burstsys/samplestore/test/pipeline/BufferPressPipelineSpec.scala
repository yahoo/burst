/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test.pipeline

import org.burstsys.brio.configuration.brioPressThreadsProperty
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.samplestore.pipeline
import org.burstsys.tesla

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

//@Ignore
class BufferPressPipelineSpec extends PressAbstractSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  it should "start buffer press in parallel" in {

    val presserSchema = BrioSchema("presser")
    val count = 1000000
    val stream = MockNexusStream("unity", expectedItemCount = count)
    val futures = for (_ <- 0 until count) yield
      pipeline.pressToFuture(stream, MockPressSource(), presserSchema,
        1, 10000000)

    Await.result(Future.sequence(futures), 1 minutes)
    tesla.buffer.factory.inUseParts should be(brioPressThreadsProperty.get)
  }

}
