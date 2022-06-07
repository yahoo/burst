/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra

import java.util.concurrent.LinkedBlockingQueue

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._

import scala.concurrent.{Future, Promise}

package object parser extends VitalsLogger {

  /**
   * pool of parsers used whenever top level hydra analysis parsing is needed
   */
  private lazy val hydraParserQueue: LinkedBlockingQueue[HydraParser] = {
    val q = new LinkedBlockingQueue[HydraParser]
    (1 to configuration.burstHydraParserCountProperty.getOrThrow) foreach (_ => q put HydraParser())
    q
  }

  /**
   * parse hydra source to create an analysis object using a pool of hydra
   * parsers.
   *
   * @param source
   * @param schema
   * @return future for the result analysis tree (sync for now but allowing for upgrade)
   */
  def parse(source: String, schema: BrioSchema): FeltAnalysisDecl = {
    val parserInstance = hydraParserQueue.take
    try {
      val start = System.nanoTime
      val analysis = parserInstance parseAnalysis(source, schema)
      HydraReporter.recordParse(System.nanoTime - start, source)
      analysis
    } catch safely {
      case t: Throwable => throw t
    } finally {
      hydraParserQueue put parserInstance
    }
  }


}
