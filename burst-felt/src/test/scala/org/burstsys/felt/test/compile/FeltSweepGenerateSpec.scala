/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.test.compile

import org.burstsys.felt.compile.FeltClassName
import org.burstsys.felt.compile.artifact.{FeltArtifactKey, FeltArtifactTag}
import org.burstsys.felt.configuration.{burstFeltMaxCachedSweepProperty, burstFeltSweepCleanSecondsProperty}
import org.burstsys.felt.test.FeltAbstractSpecSupport
import org.burstsys.felt.test.mock.{MockAnalysis, MockBinding, mockBrioSchema}
import org.burstsys.felt.{FeltListener, FeltService}

import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.language.postfixOps

//@Ignore
class FeltSweepGenerateSpec extends FeltAbstractSpecSupport with FeltListener {
  burstFeltSweepCleanSecondsProperty.set(1)
  burstFeltMaxCachedSweepProperty.set(0)

  val sweepGenerateLatch = new CountDownLatch(1)
  val sweepCleanLatch = new CountDownLatch(1)
  val classDeleteLatch = new CountDownLatch(2)

  it should "generate sweep with caching" in {

    FeltService talksTo this

    val mockSweep = FeltService.generateSweep(
      source = "test source", analysis = MockAnalysis, brioSchema = mockBrioSchema, binding = MockBinding
    )

    // TODO do an actual mock scan??

    mockSweep.artifact.releaseReadLock

    sweepGenerateLatch.await(60, TimeUnit.SECONDS) should equal(true)
    sweepCleanLatch.await(60, TimeUnit.SECONDS) should equal(true)
    classDeleteLatch.await(60, TimeUnit.SECONDS) should equal(true)
  }

  override def onFeltSweepClean(key: FeltArtifactKey, tag: FeltArtifactTag): Unit = {
    sweepCleanLatch.countDown()
  }

  override def onFeltSweepGenerate(key: FeltArtifactKey): Unit = {
    sweepGenerateLatch.countDown()
  }

  override def onFeltAddToClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, className: FeltClassName, byteCount: Int): Unit = {
  }

  override def onFeltDeleteFromClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, className: FeltClassName): Unit = {
    classDeleteLatch.countDown()
    classDeleteLatch.countDown()
  }
}
