/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.dictionary

import org.burstsys.brio.dictionary._
import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.strings._

import scala.collection.mutable

//@Ignore
class BrioFlexDictionarySpec extends BrioAbstractSpec {

  val randomWordList1: Array[String] = (for (i <- 0 until 1e4.toInt) yield randomWord(4, 12)).toArray
  val randomWordList2: Array[String] = (for (i <- 0 until 1e4.toInt) yield randomWord(4, 12)).toArray

  it should "change size correctly" in {
    TeslaWorkerCoupler {
      val flexDictionary = flex.grabFlexDictionary(builder = BrioDictionaryBuilder())
      try {

        val stringToKeyMap = new mutable.HashMap[String, BrioDictionaryKey]
        val keyToStringMap = new mutable.HashMap[BrioDictionaryKey, String]

        randomWordList1 foreach {
          word =>
            val key = flexDictionary.keyLookupWithAdd(word)
            val zk = BrioDictionaryKeyAnyVal(key)
            log debug s"adding word='$word' with key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) "
            stringToKeyMap += word -> key
            keyToStringMap += key -> word
        }

        randomWordList1.length should equal(flexDictionary.words)

        randomWordList2 foreach {
          word =>
            val key = flexDictionary.keyLookupWithAdd(word)
            val zk = BrioDictionaryKeyAnyVal(key)
            log debug s"adding word='$word' with key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) "
            stringToKeyMap += word -> key
            keyToStringMap += key -> word
        }

        val dump = flexDictionary.dump

        stringToKeyMap foreach {
          case (s, k) =>
            var key = flexDictionary.keyLookup(s)
            val zk = BrioDictionaryKeyAnyVal(key)
            log debug s"found key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) for word '$s'"
            key should equal(k)
        }

        keyToStringMap foreach {
          case (k, s) =>
            val word = flexDictionary.stringLookup(k)
            val zk = BrioDictionaryKeyAnyVal(k)
            log debug s"found string '$word'  for key $s (bucket=${zk.bucket}, ordinal=${zk.ordinal})"
            word should equal(s)
        }

      } finally {
        flex.releaseFlexDictionary(flexDictionary)
      }
    }

  }
}
