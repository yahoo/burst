/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.wave.execution.model.execute.invoke.FabricInvocation
import org.burstsys.felt.compile.FeltCompileEngine
import org.burstsys.felt.compile.artifact.{FeltArtifact, FeltArtifactKey, FeltArtifactTag, FeltArtifactory}
import org.burstsys.felt.configuration.{burstFeltMaxCachedSweepProperty, burstFeltSweepCleanSecondsProperty}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.analysis.generate.FeltAnalysisSweepGenerator
import org.burstsys.felt.model.collectors.runtime.FeltCollectorSweep
import org.burstsys.felt.model.lattice.FeltLatticeSweep
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.tree.FeltTree
import org.burstsys.felt.model.tree.code.FeltCodeCursor
import org.burstsys.felt.model.visits.runtime.FeltVisitSweep
import org.burstsys.felt.{FeltReporter, FeltService}
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}
import org.burstsys.vitals.errors.{VitalsException, safely}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
 * The per blob scan traversal group analysis 'semantics' closure passed to the top level
 * [[FeltTraveler]] which
 * traverses the brio object tree [[org.burstsys.brio.blob.BrioBlob]]
 * This needs to be implemented in any concrete instance of a [[FeltTree]] based execution model
 *
 * @see [[org.burstsys.brio.blob.BrioBlob]]
 *      [[FeltTraveler]]
 */
trait FeltSweep extends AnyRef with FeltVisitSweep with FeltLatticeSweep with FeltCollectorSweep {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private[this]
  var _artifact: FeltArtifact[_] = _

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * user friendly name for this sweep instance
   *
   * @return
   */
  def sweepName: String

  final def artifact: FeltArtifact[_] = _artifact

  final def artifact_=(artifact: FeltArtifact[_]): Unit = _artifact = artifact

  /**
   * Unique per scan traversal ID for this sweep instance
   *
   * @return
   */
  def sweepClassName: String

  /**
   * the [[FeltTraveler]] for this sweep
   *
   * @return
   */
  def feltTraveler: FeltTraveler[_]

  /**
   * the [[BrioSchema]] for this sweep
   *
   * @return
   */
  final
  def brioSchema: BrioSchema = feltTraveler.brioSchema

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // top level traversal call
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * top level scan traversal entry point
   *
   * @param runtime all sweep mutable data to use during the execution of the immutable sweep metadata
   */
  def apply(runtime: FeltRuntime): Unit

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // instantiate runtimes
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Instantiate a custom code generated runtime for this sweep. This will be passed into all calls.
   *
   * @return
   */
  def newRuntime(call: FabricInvocation): FeltRuntime

}

final case
class FeltSweepArtifact(key: FeltArtifactKey, tag: FeltArtifactTag, input: FeltAnalysisDecl) extends FeltArtifact[FeltAnalysisDecl] {

  override val name: String = "sweep"

  var sweep: FeltSweep = _
  var generatedSource: String = _

  override def delete(key: FeltArtifactKey, tag: FeltArtifactTag): Unit = {
    sweep = null
    FeltCompileEngine.deleteFromClassLoaderByTag(key, tag)
    super.delete(key, tag)
    FeltService.onFeltSweepClean(key, tag)
  }

}

object FeltSweep extends FeltArtifactory[FeltAnalysisDecl, FeltSweepArtifact] {

  override val modality: VitalsServiceModality = VitalsSingleton

  override val serviceName: String = s"felt-sweep-artifactory"

  override val lruEnabled: Boolean = true

  override lazy val cleanDuration: Duration = Duration(burstFeltSweepCleanSecondsProperty.get, TimeUnit.SECONDS)

  override protected lazy val maxCount: Int = burstFeltMaxCachedSweepProperty.get

  override protected def onCacheHit(): Unit = FeltReporter.recordSweepCacheHit()

  final override
  def createArtifact(key: FeltArtifactKey, tag: FeltArtifactTag, analysis: FeltAnalysisDecl): FeltSweepArtifact =
    FeltSweepArtifact(key, tag, analysis)

  override val artifactName = "FeltSweep"

  final
  def apply(key: FeltArtifactKey, tag: FeltArtifactTag, analysis: FeltAnalysisDecl): FeltSweep = {
    try {
      fetchArtifact(key = key, tag = tag, input = analysis).sweep
    } catch {
      case t: Throwable =>
        throw FeltException(analysis.location, s"FELT_SWEEP_GEN_APPLY_FAIL $t $tag", t)
    }
  }

  final protected override
  def generateContent(artifact: FeltSweepArtifact): Unit = {
    lazy val tag = s"FeltSweep.generateContent(key=${artifact.key})"
    startIfNotRunning
    val analysis = artifact.input

    /**
     * make sure we have a [[FeltTraveler]] - we just get the classname.
     * This is generated once per [[BrioSchema]] with some feature based variations.
     */
    analysis.global.travelerClassName = try {
      FeltTraveler(global = analysis.global)
    } catch {
      case t: Throwable =>
        throw FeltException(analysis.location, s"FELT_SWEEP_GEN_TRAVELER_FAIL $t $tag")
    }

    /**
     * now we generate the code from our analysis [[FeltTree]]
     */
    try {
      artifact.generatedSource =
        FeltAnalysisSweepGenerator(analysis = analysis).generateSweep(FeltCodeCursor(analysis.global))
    } catch safely {
      case t: Throwable =>
        throw FeltException(analysis.location, s"FELT_SWEEP_GEN_SOURCE_FAIL $t $tag", t)
    }

    /**
     * now we scala compile our generated code to create a closure class that is cached
     */
    val compilationStart = System.nanoTime
    try {
      artifact.sweep = FeltCompileEngine.generatedSourceToSweepInstance(artifact.key, artifact.tag, artifact.generatedSource) match {
        case Failure(t) =>
          throw FeltException(analysis.location, s"FELT_SWEEP_GEN_COMPILE_FAIL $t $tag")
        case Success(r) =>
          if (r.length != 1) throw VitalsException(s"FELT_SWEEP_GEN_NOT_UNIQUE (generation did not yield exactly one instance...) $tag")
          r.head.asInstanceOf[FeltSweep]
      }
      artifact.sweep.artifact = artifact // for read lock release
    } catch safely {
      case t: Throwable =>
        throw FeltException(analysis.location, s"FELT_SWEEP_GEN_FAIL $t $tag")
    } finally {
      FeltReporter.recordSweepCompile(System.nanoTime - compilationStart, artifact.generatedSource)
      FeltService.onFeltSweepGenerate(artifact.key)
    }

  }

}
