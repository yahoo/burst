/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.test.json

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.vitals.json.VitalsJsonSanatizers._
import org.burstsys.vitals.test.VitalsAbstractSpec

class VitalsHtmlSanitizerSpec extends VitalsAbstractSpec {

  private val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .build()

  it should "redact string properties" in {
    val json = mapper.writeValueAsString(JsonTestEntity("some text<script>alert('fail')</script> or other"))
    json should equal("""{"text":"some text or other","props":{},"list":[]}""")
  }

  it should "redact map entries" in {
    val json = mapper.writeValueAsString(JsonTestEntity(props = Map(
      ("script", "<script>alert('fail')</script>Burst <3s scripts"),
        ("link<a><a/>", "Links work")
    )))
    json should equal("""{"text":"","props":{"script":"Burst &lt;3s scripts","link":"Links work"},"list":[]}""")
  }

  it should "redact array entries" in {
    val json = mapper.writeValueAsString(JsonTestEntity(list = Array("Burst", "<3s", "script<script>alert('ha!')</script> tags")))
    json should equal("""{"text":"","props":{},"list":["Burst","&lt;3s","script tags"]}""")
  }
}

final case class JsonTestEntity(
                                 @JsonSerialize(using = classOf[Values]) text: String = "",
                                 @JsonSerialize(keyUsing = classOf[Keys], contentUsing = classOf[Values]) props: Map[String, String] = Map.empty,
                                 @JsonSerialize(contentUsing = classOf[Values]) list: Array[String] = Array.empty)
