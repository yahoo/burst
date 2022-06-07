/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.provider

import org.burstsys.relate.RelateService
import org.burstsys.vitals.errors.safely
import scalikejdbc.{DBSession, SQL}

/**
  * helper functions for script execution
  */
trait RelateScriptExecutor extends AnyRef with RelateService {

  self: RelateProvider =>

  final override
  def executeScript(source: String, translateEscapes: Boolean = false)(implicit session: DBSession): this.type = {
    connection localTx {
      implicit session =>
        val builder = new StringBuilder
        Predef.augmentString(source).lines foreach (executeLine(builder, _, translateEscapes))
        val remainder = builder.result().trim
        if (!remainder.isEmpty)
          throw new RuntimeException(s"SQL: script not terminated by semicolon - remainder '$remainder'")
    }
    this
  }

  private
  def executeLine(builder: StringBuilder, line: String, translateEscapes: Boolean)(implicit session: DBSession): this.type = {
    var text = line.trim
    if (text.isEmpty)
      return this
    if (text.startsWith("#"))
      return this
    if (text.startsWith("--"))
      return this

    val trailingSemiColon = text.lastIndexOf(';')
    if (trailingSemiColon != -1 && trailingSemiColon == text.length - 1) {
      try {
        text = text.substring(0, trailingSemiColon)
        builder ++= text + " "
        var scriptlet = builder.result()
        scriptlet = scriptlet.replaceAll("\\\\n", "\n")
        log info s"SQL: execute scriptlet: \n\t$scriptlet"
        SQL(scriptlet).execute().apply()
        builder.clear()
      } catch safely {
        case e: Exception =>
          log warn s"$burstModuleName exception:${e.toString}"
          throw e
      }
    } else {
      builder ++= text + " "
    }
    this
  }


}
