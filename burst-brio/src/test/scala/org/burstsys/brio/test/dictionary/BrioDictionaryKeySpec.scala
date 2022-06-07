/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.dictionary

import org.burstsys.brio.dictionary
import org.burstsys.brio.dictionary.container._
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal

//@Ignore
class BrioDictionaryKeySpec extends BrioAbstractSpec {



  "Zap Keys" should "store and retrieve bucket" in {
    var k = BrioDictionaryKeyAnyVal()
    k = k.bucket(33)
    k = BrioDictionaryKeyAnyVal(k.data)
    k.bucket should equal(33)
  }

  "Zap Keys" should "store and retrieve ordinal" in {
    var k = BrioDictionaryKeyAnyVal()
    k = k.ordinal(33)
    k = BrioDictionaryKeyAnyVal(k.data)
    k.ordinal should equal(33)
  }

  "Zap Keys" should "store and retrieve bucket and ordinal" in {
    var k = BrioDictionaryKeyAnyVal()
    k = k.ordinal(33)
    k = k.bucket(12)
    k = BrioDictionaryKeyAnyVal(k.data)
    k.ordinal should equal(33)
    k.bucket should equal(12)
  }

  "Zap Keys" should "reject bucket size just right" in {
    var k = BrioDictionaryKeyAnyVal()
    k = k.bucket(BucketCount - 1)
  }

  "Zap Keys" should "reject bucket size too large" in {
    var k = BrioDictionaryKeyAnyVal()
    val thrown = intercept[RuntimeException] {
      k = k.bucket(BucketCount)
    }
    thrown.getLocalizedMessage should include("bucket size larger")
  }

  "Zap Keys" should "reject bucket size too small" in {
    var k = BrioDictionaryKeyAnyVal()
    val thrown = intercept[RuntimeException] {
      k = k.bucket(-1)
    }
    thrown.getLocalizedMessage should include("bucket size larger")
  }

  "Zap Keys" should "reject ordinal size just right" in {
    var k = BrioDictionaryKeyAnyVal()
    k = k.ordinal(MaxOrdinal - 1)
  }

  "Zap Keys" should "reject ordinal size too large" in {
    var k = BrioDictionaryKeyAnyVal()
    val thrown = intercept[RuntimeException] {
      k = k.ordinal(MaxOrdinal)
    }
    thrown.getLocalizedMessage should include("ordinal size larger")
  }

  "Zap Keys" should "reject ordinal size too small" in {
    var k = BrioDictionaryKeyAnyVal()
    val thrown = intercept[RuntimeException] {
      k = k.ordinal(-1)
    }
    thrown.getLocalizedMessage should include("ordinal size larger")
  }

}
