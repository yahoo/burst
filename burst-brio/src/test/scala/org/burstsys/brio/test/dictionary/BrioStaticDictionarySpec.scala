/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.dictionary

import org.burstsys.brio.dictionary._
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.tesla.offheap
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.text.VitalsTextCodec

import scala.collection.mutable

//@Ignore
class BrioStaticDictionarySpec extends BrioAbstractSpec {

  it should "export as a block to another dictionary correctly" in {
    TeslaWorkerCoupler(for (i <- 0 until TeslaWorkerCoupler.workerCount) test())
  }

  def test(): Unit = {
    val sourceDictionary = factory.grabMutableDictionary()
    val destinationDictionary = factory.grabMutableDictionary()

    try {
      //      sourceDictionary.availableMemorySize should equal(33554423)

      val stringToKeyMap = new mutable.HashMap[String, BrioDictionaryKey]
      val keyToStringMap = new mutable.HashMap[BrioDictionaryKey, String]
      randomWordList foreach {
        word =>
          val key = sourceDictionary.keyLookupWithAdd(word)
          val zk = BrioDictionaryKeyAnyVal(key)
          log debug s"adding word='$word' with key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) "
          stringToKeyMap += word -> key
          keyToStringMap += key -> word
      }

      stringToKeyMap foreach {
        case (s, k) =>
          var key = sourceDictionary.keyLookup(s)
          val zk = BrioDictionaryKeyAnyVal(key)
          log debug s"found key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) for word '$s'"
          key should equal(k)
      }

      keyToStringMap foreach {
        case (k, s) =>
          val word = sourceDictionary.stringLookup(k)
          val zk = BrioDictionaryKeyAnyVal(k)
          log debug s"found string '$word'  for key $s (bucket=${zk.bucket}, ordinal=${zk.ordinal})"
          word should equal(s)
      }

      randomWordList.length should equal(sourceDictionary.words)

      val foo = sourceDictionary.dump(VitalsTextCodec())

      /**
       * now we have a mutable dictionary, export to a static dictionary and try to read back all same keys and values.
       */

      val destinationPtr = destinationDictionary.basePtr

      // dictionary size
      val dictionaryMemorySize = sourceDictionary.currentMemorySize

      // dictionary data
      var i = 0
      while (i <= dictionaryMemorySize) {
        offheap.putByte(destinationPtr + i, offheap.getByte(sourceDictionary.basePtr + i))
        i += 1
      }

      destinationDictionary.words should equal(sourceDictionary.words)

      destinationDictionary.currentMemorySize should equal(sourceDictionary.currentMemorySize)

      stringToKeyMap foreach {
        case (s, k) =>
          var key = destinationDictionary.keyLookup(s)
          val zk = BrioDictionaryKeyAnyVal(key)
          log debug s"found key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) for word '$s'"
          key should equal(k)
      }

      keyToStringMap foreach {
        case (k, s) =>
          val word = destinationDictionary.stringLookup(k)
          val zk = BrioDictionaryKeyAnyVal(k)
          log debug s"found string '$word'  for key $s (bucket=${zk.bucket}, ordinal=${zk.ordinal})"
          word should equal(s)
      }
    } finally {
      factory.releaseMutableDictionary(sourceDictionary)
      factory.releaseMutableDictionary(destinationDictionary)
    }

  }
}
