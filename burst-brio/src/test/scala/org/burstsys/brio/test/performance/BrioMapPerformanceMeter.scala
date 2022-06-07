/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test.performance

import org.burstsys.brio.test.BrioAbstractSpec
import gnu.trove.map.hash.TObjectIntHashMap

object BrioMapPerformanceMeter extends BrioAbstractSpec {


  val troveMap = new TObjectIntHashMap[String]
  troveMap.put("field1", 450897)
  troveMap.put("field2", 826)
  troveMap.put("field3", 100277)
  troveMap.put("field4", 8884098)
  troveMap.put("field5", 2039486)

  val scalaMap = new scala.collection.mutable.HashMap[String, Int]

  scalaMap.put("field1", 450897)
  scalaMap.put("field2", 826)
  scalaMap.put("field3", 100277)
  scalaMap.put("field4", 8884098)
  scalaMap.put("field5", 2039486)

  val array = Array(450897, 826)

  val start = System.nanoTime()
  var v: Int = 0

  val count: Double = Long.MaxValue
  var i = 0
  while (i < count.toInt) {
    var j = 0
    while (j < count.toInt) {
      //            v = scalaMap.get("field1").get // TODO 17 seconds
      v = troveMap.get("field1") // TODO 9.5 seconds
      //                  v = array(1) // TODO 1.2 seconds
      j += 1
    }
    Thread.sleep(100)
    i += 1
  }

  if (v > 10) log.info("v=%,d".format(v))

  val end = System.nanoTime()

  log.info("%,f read(s) elapsed %s, %s per read".format(
    count,
    org.burstsys.vitals.instrument.prettyTimeFromNanos(end - start),
    org.burstsys.vitals.instrument.prettyTimeFromNanos((end - start) / count))
  )

}
