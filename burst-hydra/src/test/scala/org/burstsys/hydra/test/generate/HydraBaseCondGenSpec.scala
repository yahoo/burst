/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.generate

import org.burstsys.hydra.test.support.HydraSpecSupport

//@Ignore
class HydraBaseCondGenSpec extends HydraSpecSupport {

  it should "base condition generation 1" in {
    implicit val source: String =
      """
        |if(true) {
        |  ???
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "base condition generation 2" in {
    implicit val source: String =
      """
        |if(true) {
        |  ???
        |} else {
        |  ???
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "base condition generation 3" in {
    implicit val source: String =
      """
        |if(true) {
        |  ???
        |} else if(false) {
        |  ???
        |} else {
        |  ???
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "base condition generation 4" in {
    implicit val source: String =
      """
        |if(true) {
        |  1
        |} else if(false) {
        |  2
        |} else if(false) {
        |  3
        |} else {
        |  4
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "base condition generation 5" in {
    implicit val source: String =
      """
        |if(true) {
        |  2 + 5 * 4
        |} else if(false) {
        |  user.sessions.id * 2
        |} else if(false) {
        |  ???
        |} else {
        |  4
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "base condition generation 6" in {
    implicit val source: String =
      """
        |if(true) {
        |  user.sessions.id * 2
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "base condition generation 7" in {
    implicit val source: String =
      """
        |if(true) {
        |   var foo:integer = 45
        |   user.sessions.id * foo
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

  it should "base condition generation 8" in {
    implicit val source: String =
      """
        |if(true) {
        |   var foo:integer = 45
        |}
      """.stripMargin
    val foo = parser printGeneration (_.parseAnalysis(wrap, schema))
    foo
  }

}
