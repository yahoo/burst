/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.dictionary

import org.burstsys.brio
import org.burstsys.brio.dictionary.key.BrioDictionaryKeyAnyVal
import org.burstsys.brio.types.BrioTypes.BrioDictionaryKey
import org.burstsys.tesla.thread.worker.TeslaWorkerCoupler
import org.burstsys.vitals.text.VitalsTextCodec

import scala.collection.mutable

//@Ignore
class BrioDictionaryPoolSpec extends BrioAbstractSpec {


  it should "initialize and lookup a single stored word" in {
    TeslaWorkerCoupler {
      implicit val text: VitalsTextCodec = VitalsTextCodec() // OK
      val start = System.nanoTime

      for (i <- 0 until 10) {
        val d = brio.dictionary.factory.grabMutableDictionary()
        try {
          val stringToKeyMap = new mutable.HashMap[String, BrioDictionaryKey]
          val keyToStringMap = new mutable.HashMap[BrioDictionaryKey, String]
          randomWordList.take(100) foreach {
            word =>
              val key = d.keyLookupWithAdd(word)
              var zk = BrioDictionaryKeyAnyVal(key)
              //            log debug s"adding word='$word' with key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) "
              stringToKeyMap += word -> key
              keyToStringMap += key -> word
          }

          stringToKeyMap foreach {
            case (s, k) =>
              var key = d.keyLookup(s)
              var zk = BrioDictionaryKeyAnyVal(key)
              //            log debug s"found key $key (bucket=${zk.bucket}, ordinal=${zk.ordinal}) for word '$s'"
              key should equal(k)
          }

          keyToStringMap foreach {
            case (k, s) =>
              var word = d.stringLookup(k)
              var zk = BrioDictionaryKeyAnyVal(k)
              //            log debug s"found string '$word'  for key $s (bucket=${zk.bucket}, ordinal=${zk.ordinal})"
              word should equal(s)
          }

          /*
                log info f"successfully mapped ${randomWordList.length}%,d word(s) ${
                  prettyRateString("word(s)", randomWordList.length, System.nanoTime - start)
                }"
        */
        } finally brio.dictionary.factory.releaseMutableDictionary(d)
      }
    }

  }


}
