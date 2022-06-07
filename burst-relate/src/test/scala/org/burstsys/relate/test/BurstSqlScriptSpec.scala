/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.test

import org.burstsys.relate.test.support.{BurstSqlSpecLog, BurstSqlSpecSupport}
import org.burstsys.vitals.logging._
import org.apache.logging.log4j.Logger
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 */
//@Ignore
class BurstSqlScriptSpec extends AnyFlatSpec with Matchers with BurstSqlSpecLog with BurstSqlSpecSupport {

  "Burst SQL Script" should "succeed" in {
    sqlTest {
      persister =>
        persister.connection localTx {
          implicit session =>
            val sql = persister.service
            getClass.getResourceAsStream("/burst-relate-test.sql") match {
              case null =>
                throw new RuntimeException
              case (in) =>
                val source = scala.io.Source.fromInputStream(in).getLines().mkString("\n")
                sql.executeScript(source)
            }
            val list = persister.fetchAllEntities()
            list.size should equal(3)
        }
    }
  }

}
