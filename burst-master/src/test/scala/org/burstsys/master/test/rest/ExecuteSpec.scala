/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.test.rest

import com.fasterxml.jackson.databind.JsonNode
import org.burstsys.master.test.support.BurstMasterSpecSupport
import org.burstsys.vitals.errors.safely

class ExecuteSpec extends BurstMasterSpecSupport {

  private lazy val over = Map[String, String]("domain" -> s"${domain.pk}", "view" -> s"${views.head.pk}", "timezone" -> "GMT")

  private val schema = "unity"

  private def getFirstRowFromDash(query: String, args: Option[String] = None): JsonNode = {
    val results = fetchObjectFrom("/query/executeGroup", "POST", over ++ Map("source" -> query) ++ args.map(a => Map("args" -> a)).getOrElse(Map()))
    results.path("resultGroup").path("resultStatus").path("isSuccess").asBoolean() should be(true)
    results.path("resultGroup").path("resultSets").path("0").path("rowCount").asLong() should equal(1)
    results.path("resultGroup").path("resultSets").path("0").path("rowSet").get(0)
  }

  "executeGroup endpoint" should "execute hydra with no parameters" in {
    // on SD the first fetch fails so ignore it
    try {
      getFirstRowFromDash(s"select cast(10 as long) as vl from schema $schema")
    } catch safely {
      case _: Exception =>
    }
    val row = getFirstRowFromDash(s"select 10 as vl from schema $schema")
    row.path("cells").get(0).path("bData").asLong should equal(10)
  }

  it should "execute hydra with one long parameter" in {
    val row = getFirstRowFromDash(s"select(v: long) $$v as vl from schema $schema", Some("""{"v": 10}"""))
    row.path("cells").get(0).path("bData").asLong should equal(10)
  }

  it should "execute hydra with one integer parameter" in {
    val row = getFirstRowFromDash(s"select (v: integer) $$v as vl from schema $schema", Some("""{"v: integer": 10}"""))
    row.path("cells").get(0).path("bData").asLong should equal(10)
  }

  // TODO LEXICONS have broken string parameter literals
  ignore should "execute hydra with one string parameter" in {
    val row = getFirstRowFromDash(s"select (v: string) $$v as vl from schema $schema", Some("""{"v": "hello"}"""))
    row.path("cells").get(0).path("bData").asText should equal("hello")
  }

  it should "execute hydra with one double parameter" in {
    val row = getFirstRowFromDash(s"select (v: double) $$v as vl from schema $schema", Some("""{"v": 10.10}"""))
    row.path("cells").get(0).path("bData").asDouble should equal(10.10)
  }

  it should "execute hydra with multiple parameters" in {
    val row = getFirstRowFromDash(
      s"select (v1: integer, v2: long, v3: double) $$v1 as one, $$v2 as two, $$v3 as three from schema $schema",
      Some("""{"v1: integer": 10, "v2": 20, "v3": 40.40}""")
    )
    val cell = row.path("cells")
    cell.get(0).path("bData").asInt should equal(10)
    cell.get(1).path("bData").asLong should equal(20)
    cell.get(2).path("bData").asDouble should equal(40.40)
  }

}
