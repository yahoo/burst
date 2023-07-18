/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.dictionary

import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal
import org.burstsys.brio.dictionary.mutable.{BrioMutableDictionary, BrioMutableDictionaryAnyVal}
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.tesla
import org.burstsys.tesla.block.factory.TeslaBlockSizes.findBlockSize
import org.burstsys.tesla.block.{TeslaBlock, TeslaBlockAnyVal}
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.reporter.instrument._

import scala.collection.mutable

//@Ignore
class BrioDictionarySpec extends BrioAbstractSpec {

  var w1 = "potato"

  "Zap Dictionaries" should "initialize and lookup a single stored word" in {
    TeslaWorkerCoupler {
      val size =100000
      val blockPtr = tesla.block.factory.grabBlock(size).blockBasePtr
      TeslaBlockAnyVal(blockPtr).dataSize should be <=findBlockSize(size)
      val d = BrioMutableDictionaryAnyVal(blockPtr)
      d.initialize(1, BrioDictionaryBuilder())
      d.words should equal(0)
      val k1 = d.keyLookupWithAdd(w1)
      d.stringLookup(k1) should equal(w1)
    }
  }

  "Zap Dictionaries" should "create and lookup a set of words" in {
    TeslaWorkerCoupler {

      val start = System.nanoTime

      val blockPtr = tesla.block.factory.grabBlock(1000000).blockBasePtr
      val d = BrioMutableDictionaryAnyVal(blockPtr)
      d.initialize(1,BrioDictionaryBuilder())
      val stringToKeyMap = new mutable.HashMap[String, BrioDictionaryKey]
      val keyToStringMap = new mutable.HashMap[BrioDictionaryKey, String]
      randomWordList foreach {
        word =>
          val key = d.keyLookupWithAdd(word)
          var zk = BrioDictionaryKeyAnyVal(key)
          log debug s"adding word='$word' with key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) "
          stringToKeyMap += word -> key
          keyToStringMap += key -> word
      }

      stringToKeyMap foreach {
        case (s, k) =>
          var key = d.keyLookup(s)
          var zk = BrioDictionaryKeyAnyVal(key)
          log debug s"found key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) for word '$s'"
          key should equal(k)
      }

      keyToStringMap foreach {
        case (k, s) =>
          var word = d.stringLookup(k)
          var zk = BrioDictionaryKeyAnyVal(k)
          log debug s"found string '$word'  for key $s (bucket=${zk.bucket}, ordinal=${zk.ordinal})"
          word should equal(s)
      }

      log info f"successfully mapped ${randomWordList.length}%,d word(s) ${
        prettyRateString("word(s)", randomWordList.length, System.nanoTime - start)
      }"
    }

  }

  "Zap Dictionaries" should "create a dictionary that loads too many words" in {
    TeslaWorkerCoupler {
      val blockPtr = tesla.block.factory.grabBlock(200).blockBasePtr
      val d = BrioMutableDictionaryAnyVal(blockPtr)
      d.initialize(1,BrioDictionaryBuilder())
      var i = 0
      while (i < randomWordList.length && !d.overflowed) {
        d.keyLookupWithAdd(randomWordList(i))
        i += 1
      }
      d.overflowed should equal(true)
    }

  }

}
