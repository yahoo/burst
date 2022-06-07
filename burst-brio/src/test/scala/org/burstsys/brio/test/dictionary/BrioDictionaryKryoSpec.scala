/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.dictionary

import com.esotericsoftware.kryo.io.{Input, Output}
import org.burstsys.brio.dictionary._
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.tesla.configuration
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.errors._
import org.burstsys.vitals.kryo.{acquireKryo, releaseKryo}
import org.burstsys.vitals.text.VitalsTextCodec

import scala.collection.mutable

//@Ignore
class BrioDictionaryKryoSpec extends BrioAbstractSpec {


  it should "kryo codec" in {
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

      try {
        val k = acquireKryo
        try {
          val output = new Output(1e6.toInt)
          sourceDictionary.write(k, output)
          val encoded = output.toBytes

          val input = new Input(encoded)
          destinationDictionary.read(k, input)

        } finally releaseKryo(k)
      } catch safely {
        case t: Throwable => throw VitalsException(t)
      }

      val foo = destinationDictionary.dump(VitalsTextCodec())

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
