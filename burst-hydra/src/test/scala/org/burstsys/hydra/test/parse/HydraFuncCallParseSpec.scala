/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.parse

import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

@Ignore
class HydraFuncCallParseSpec extends HydraSpecSupport {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Examples
  ////////////////////////////////////////////////////////////////////////////////////////////////////


  it should "parse function call 1" in {
    implicit val source: String =
      s"""{
         |  size(user.sessions.events)
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(source, schema))
  }

  it should "parse function call 2" in {
    implicit val source: String =
      s"""{
         |  contains(keys())
         |  contains(values())
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(source, schema))
  }

  it should "parse function call 3" in {
    implicit val source: String =
      s"""{
         |  enum()
         |  split()
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(source, schema))
  }

  it should "parse function call 4" in {
    implicit val source: String =
      s"""{
         |  secondGrain()
         |  minuteGrain()
         |  hourGrain()
         |  dayGrain()
         |  weekGrain()
         }
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(source, schema))
  }

  it should "parse function call 5" in {
    implicit val source: String =
      s"""{
         |  calendarYearGrain()
         |  calendarHalfGrain()
         |  calendarQuarterGrain()
         |  calendarMonthGrain()
         |  calendarWeekGrain()
         |  calendarDayGrain()
         |  calendarHourGrain()
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(source, schema))
  }

  it should "parse function call 6" in {
    implicit val source: String =
      s"""{
         |  hourOfDayOrdinal()
         |  dayOfWeekOrdinal()
         |  weekOfYearOrdinal()
         |  dayOfMonthOrdinal()
         |  monthOfYearOrdinal()
         |  calendarHalfOrdinal()
         |  year()
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(source, schema))
  }

  it should "parse function call 7" in {
    implicit val source: String =
      s"""{
         |  cast(use.sessions.startTime as string)
         |}
       """.stripMargin

    val expr = parser printParse (_.parseAnalysis(source, schema))
  }

}
