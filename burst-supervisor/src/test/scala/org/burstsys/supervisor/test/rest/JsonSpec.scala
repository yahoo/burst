/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class TestClass(first: String = "first", second: Int = 2, third: Any = null)

trait TestTrait {
  def first: String

  def second: Int

  def third: Any
}

class JsonSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  it should "(de)serialize case classes" in {
    val toJson = mapper.writeValueAsString(TestClass())
    toJson should equal("""{"first":"first","second":2,"third":null}""")

    val fromJson = mapper.readValue(toJson, classOf[TestClass])
    fromJson should equal(TestClass())
  }

  it should "(de)serialize anonymous instances of traits" in {
    val toJson = mapper.writeValueAsString(new TestTrait {
      override val first: String = "first"
      override val second: Int = 2
      override val third: Any = null
    })
    toJson should equal("""{"first":"first","second":2,"third":null}""")
  }

}
