/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.compile.{FeltClassName, FeltCompileEngine}
import org.burstsys.felt.compile.artifact.FeltArtifactTag
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.felt.model.tree.FeltTree
import org.burstsys.vitals.VitalsService
import org.burstsys.vitals.VitalsService.VitalsSingleton
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging.VitalsLogger

/**
 * == FELT Services ==
 * Singleton service object to support FELT pipeline
 */
object FeltService extends VitalsService with VitalsLogger with FeltTalker {

  override def modality: VitalsService.VitalsServiceModality = VitalsSingleton

  override def serviceName: String = s"felt"

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // GENERATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param source     the language source (this also used as the cache identity of the resulting sweep closure)
   * @param analysis   the [[FeltAnalysisDecl]] that is the parsed [[FeltTree]] root
   * @param brioSchema the default schema for this analysis (it can be overriden by source)
   * @param binding    the set of concrete language bindings used by Felt to property build/validate/generate the tree into code source
   * @tparam T
   * @return
   */
  def generateSweep[T <: FeltSweep](source: String, analysis: FeltAnalysisDecl, brioSchema: BrioSchema, binding: FeltBinding): FeltSweep = {
    try {
      val global = analysis.global
      global.bind(brioSchema)
      FeltSweep(key = source, tag = analysis.sweepClassName, analysis = analysis)
    } catch safely {
      case t: Throwable => throw t
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    if (isRunning) return this
    log info startingMessage
    FeltCompileEngine.start
    FeltTraveler.start
    FeltSweep.start
    markRunning
    this
  }

  override
  def stop: this.type = {
    if (!isRunning) return this
    log info stoppingMessage
    FeltCompileEngine.stop
    FeltTraveler.stop
    FeltSweep.stop
    markNotRunning
    this
  }

}
