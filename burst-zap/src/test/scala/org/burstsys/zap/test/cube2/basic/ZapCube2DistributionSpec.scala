/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.test.cube2.basic

import org.burstsys.vitals
import org.burstsys.zap.cube2
import org.burstsys.zap.cube2.ZapCube2Builder
import org.burstsys.zap.test.cube2.ZapCube2Spec
import org.scalatest.Ignore

/**
  * this is not really a test - its run to verify decent hashing in buckets
  */
@Ignore
class ZapCube2DistributionSpec extends ZapCube2Spec {

  val builder: ZapCube2Builder = cube2.ZapCube2Builder(dimensionCount = 2, aggregationCount = 2,
    defaultStartSize = 10e6.toInt)

  private def testDistribution(rows: Int, buckets: Int): Unit = {
    // TODO builder.bucketCount = buckets
    CubeTest {
      defineRandom(cubeA, rows)
      val bucketStdDeviation = cubeA.bucketStdDeviation
      val bucketListLengthMax = cubeA.bucketListLengthMax
      log info s"RANDOM  $rows rows, $buckets buckets ($bucketListLengthMax, ${vitals.instrument.prettyFloatNumber(bucketStdDeviation)}) (length, deviation)"
      //      distribution should be(8.0 +- 2.0)
    }
    CubeTest {
      defineAscending(cubeA, rows)
      val bucketStdDeviation = cubeA.bucketStdDeviation
      val bucketListLengthMax = cubeA.bucketListLengthMax
      log info s"ASCEND  $rows rows, $buckets buckets ($bucketListLengthMax, ${vitals.instrument.prettyFloatNumber(bucketStdDeviation)}) (length, deviation)"
      //      distribution should be(8.0 +- 2.0)
    }
  }

  it should "distribution for 10 random rows in 4 buckets " in {
    testDistribution(10, 4)
  }

  it should "distribution for 10 random rows in 8 buckets " in {
    testDistribution(10, 8)
  }

  it should "distribution for 10 random rows in 16 buckets " in {
    testDistribution(10, 16)
  }

  it should "distribution for 10 random rows in 32 buckets " in {
    testDistribution(10, 32)
  }
  it should "distribution for 10 random rows in 64 buckets " in {
    testDistribution(10, 64)
  }

  it should "distribution for 1000 random rows in 16 buckets " in {
    testDistribution(1000, 16)
  }

  it should "distribution for 1000 random rows in 32 buckets " in {
    testDistribution(1000, 32)
  }

  it should "distribution for 1000 random rows in 64 buckets " in {
    testDistribution(1000, 64)
  }

  it should "distribution for 10000 random rows in 64 buckets " in {
    testDistribution(10000, 64)
  }

  it should "distribution for 10000 random rows in 128 buckets " in {
    testDistribution(10000, 128)
  }

  it should "distribution for 100000 random rows in 128 buckets " in {
    testDistribution(100000, 128)
  }

  it should "distribution for 100000 random rows in 256 buckets " in {
    testDistribution(100000, 256)
  }


}
