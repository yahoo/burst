/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.json

import com.fasterxml.jackson.core.`type`.TypeReference
import org.burstsys.vitals
import org.burstsys.vitals.test.VitalsAbstractSpec

import scala.concurrent.duration.{Duration, DurationInt}

case class Timer(duration: Duration)

class VitalsJsonDurationSerDeSpec extends VitalsAbstractSpec {

  private val mapper = vitals.json.buildJsonMapper

  "Vitals Json" should "serialize durations" in {
    val json = mapper.writeValueAsString(Timer(1.minute))
    json should equal("""{"duration":"1 minute"}""")

    val json2 = mapper.writeValueAsString(Map(1.second -> "1 second"))
    json2 should equal("""{"1 second":"1 second"}""")
  }

  it should "deserialize durations" in {
    val d: Timer = mapper.readValue("""{"duration": "30 seconds"}""", classOf[Timer])
    d should equal(Timer(30.seconds))

    val m: Map[Duration, String] = mapper.readValue("""{"1 second": "1 second"}""", new TypeReference[Map[Duration, String]] {})
    m should equal(Map(1.second -> "1 second"))
  }

}
