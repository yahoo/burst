/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.convert

import org.burstsys.alloy.views.quo.over_quo_canned
import org.burstsys.hydra.test.support.HydraAlloyTestRunner

final
class HydraQuoStringToDoubleSpec extends HydraAlloyTestRunner {

  it should "cast string parameter values to doubles" in {
    val source =
      s"""
         |hydra MyAnalysis() {
         |  schema quo
         |  frame MyFrame  {
         |    cube user {
         |      limit = 999999
         |      dimensions {
         |        'cast':verbatim[double]
         |      }
         |    }

         |    user.sessions.events.parameters ⇒ {
         |      situ ⇒ {
         |         MyFrame.'cast' = cast( value(user.sessions.events.parameters) as double )
         |        insert( MyFrame)
         |      }
         |    }
         |
         |  }
         |}""".stripMargin

    test(
      hydra = source,
      ds = over_quo_canned,
      validate = {
        (name, result) =>
          result.resultName match {
            case "MyFrame" =>
              val found = (result.rowSet.map {
                row =>
                  row[Double]("cast")
              }).sorted

              found should equal(expected)
            case _ =>
          }
      },
      staticSweep = None // Some(new B0594910A48404B29A6B204AFB8A0AE39)
    )

  }

  def expected: Array[Double] =
    Array(
      0.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0,
      21.0, 22.0, 23.0, 25.0, 30.0, 34.0, 35.0, 36.0, 40.0, 42.0, 43.0, 44.0, 45.0, 46.0, 47.0, 48.0, 49.0, 50.0, 55.0,
      60.0, 65.0, 70.0, 75.0, 80.0, 90.0, 92.0, 100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 140.0, 150.0, 160.0, 175.0,
      180.0, 185.0, 190.0, 200.0, 210.0, 225.0, 240.0, 245.0, 250.0, 275.0, 300.0, 320.0, 325.0, 350.0, 375.0, 400.0,
      410.0, 425.0, 440.0, 450.0, 460.0, 500.0, 505.0, 525.0, 550.0, 560.0, 590.0, 600.0, 625.0, 640.0, 650.0, 675.0,
      700.0, 750.0, 770.0, 790.0, 800.0, 850.0, 900.0, 1000.0, 1050.0, 1060.0, 1100.0, 1120.0, 1150.0, 1190.0, 1200.0,
      1250.0, 1300.0, 1350.0, 1400.0, 1450.0, 1500.0, 1540.0, 1600.0, 1650.0, 1700.0, 1800.0, 1850.0, 1875.0, 2000.0,
      2100.0, 2250.0, 2300.0, 2375.0, 2380.0, 2400.0, 2500.0, 2600.0, 2700.0, 2800.0, 2900.0, 2950.0, 3000.0, 3200.0,
      3250.0, 3300.0, 3400.0, 3600.0, 3650.0, 3700.0, 3750.0, 4000.0, 4200.0, 4500.0, 4760.0, 4800.0, 5000.0, 5200.0,
      5800.0, 6000.0, 6250.0, 6400.0, 6800.0, 7200.0, 7400.0, 7500.0, 8000.0, 8400.0, 9000.0, 9600.0, 10000.0, 11345.0,
      12000.0, 12400.0, 12500.0, 14400.0, 15000.0, 16000.0, 16200.0, 18000.0, 19200.0, 20000.0, 24000.0, 25000.0,
      32000.0, 40000.0, 80000.0, 100000.0
    )

}


