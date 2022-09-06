/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.validations

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.FeltException
import org.burstsys.hydra.parser.HydraParser
import org.burstsys.hydra.test.support.HydraSpecSupport
import org.scalatest.Ignore

/**
 * ← =>
 */
@Ignore
class HydraVisitValidationSpec extends HydraSpecSupport {

  it should "check for inappropriate before in reference scalar" in {
    val text =
      s"""
        user => {
          before => {
            ???
          }
        }
       """.stripMargin
    val caught = intercept[FeltException] {
      HydraParser().parseAnalysis(text, unitySchema)
    }
    caught.getMessage should include("inappropriate action type")
  }

  it should "check for inappropriate after in reference scalar" in {
    val text =
      s"""
        user => {
          after => {
            ???
          }
        }
       """.stripMargin
    val caught = intercept[FeltException] {
      HydraParser().parseAnalysis(text, unitySchema)
    }
    caught.getMessage should include("inappropriate action type")
  }

  it should "check for inappropriate situ in reference scalar" in {
    val text =
      s"""
        user => {
          situ => {
            ???
          }
        }
       """.stripMargin
    val caught = intercept[FeltException] {
      HydraParser().parseAnalysis(text, unitySchema)
    }
    caught.getMessage should include("inappropriate action type")
  }

  it should "check for inappropriate situ in reference vector" in {
    val text =
      s"""
        user.sessions => {
          situ => {
            ???
          }
        }
       """.stripMargin
    val caught = intercept[FeltException] {
      HydraParser().parseAnalysis(text, unitySchema)
    }
    caught.getMessage should include("inappropriate action type")
  }

  it should "check for appropriate pre, post in reference scalar" in {
    val text =
      s"""
        user => {
             pre => {
               ???
             }
             post => {
               ???
             }
        }
       """.stripMargin
    HydraParser().parseAnalysis(text, unitySchema)
  }

  it should "check for appropriate before, after, pre, post in reference vector" in {
    val text =
      s"""
        user.sessions => {
             before => {
               ???
             }
             after => {
               ???
             }
             pre => {
               ???
             }
             post => {
               ???
             }
        }
       """.stripMargin
    HydraParser().parseAnalysis(text, unitySchema)
  }

  it should "check for appropriate before, after, situ in value map" in {
    val text =
      s"""
        user.sessions.events.parameters => {
             before => {
               ???
             }
             after => {
               ???
             }
             situ => {
               ???
             }
        }
       """.stripMargin
    HydraParser().parseAnalysis(text, unitySchema)
  }

  it should "check for appropriate before, after, situ in value vector" in {
    val text =
      s"""
        user.interests => {
             before => {
               ???
             }
             after => {
               ???
             }
             situ => {
               ???
             }
        }
       """.stripMargin
    HydraParser().parseAnalysis(text, unitySchema)
  }

}
