/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema.traveler

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.FeltReporter
import org.burstsys.felt.compile.FeltCompileEngine
import org.burstsys.felt.compile.artifact.{FeltArtifact, FeltArtifactKey, FeltArtifactTag, FeltArtifactory}
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.FeltCodeCursor
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}

import scala.util.{Failure, Success}

/**
 * A FELT schema 'traveler' is the underlying code generated traversal of a specific
 * Brio Schema object tree data model
 */
trait FeltTraveler[R <: FeltRuntime] extends AnyRef {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private[this]
  var _artifact: FeltArtifact[_] = _

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The actual scala 'classname' of the generated traveler class
   *
   * @return
   */
  def travelerClassName: String

  /**
   * the actual scala 'classname' of the generated schema runtime base type
   *
   * @return
   */
  def runtimeClassName: String

  /**
   * The Brio Schema associated with this Felt Schema
   *
   * @return
   */
  def brioSchema: BrioSchema

  final def artifact: FeltArtifact[_] = _artifact

  final def artifact_=(artifact: FeltArtifact[_]): Unit = _artifact = artifact

  /**
   * this is called to scan traverse a brio object tree blob
   *
   * @param runtime the per traversal runtime support structure
   * @param sweep   the sweep to run through this scan traversal
   */
  def apply(runtime: R, sweep: FeltSweep): Unit

  /**
   * Is the given path in the scope (on axis and below in the traversal)
   * defined by a second root path key
   *
   * @param visitKey
   * @param scopeKey
   * @return
   */
  def visitInScope(visitKey: BrioPathKey, scopeKey: BrioPathKey): Boolean = true

}

object FeltTraveler extends FeltArtifactory[FeltGlobal, FeltTravelerArtifact] {

  override def modality: VitalsServiceModality = VitalsSingleton

  override def serviceName: String = s"felt-traveler-artifactory"

  override val lruEnabled: Boolean = false

  override protected def maxCount: Int = 10

  implicit val index: Int = 0

  override protected def onCacheHit(): Unit = FeltReporter.recordTravelerCacheHit()

  override def createArtifact(key: FeltArtifactKey, tag: FeltArtifactTag, global: FeltGlobal): FeltTravelerArtifact =
    FeltTravelerArtifact(key, tag, global)

  override val artifactName = "FeltSchema"

  def apply(global: FeltGlobal): String = {
    val tag = global.feltSchema.name
    fetchArtifact(key = tag, tag = tag, input = global).travelerClassName
  }

  protected override
  def generateContent(artifact: FeltTravelerArtifact): Unit = {
    startIfNotRunning
    val generationStart = System.nanoTime
    try {
      val generator = FeltTravelerGenerator(
        travelerClassName = artifact.travelerClassName,
        runtimeClassName = artifact.runtimeClassName,
        brioSchema = artifact.input.brioSchema
      )
      artifact.generatedSource = generator.generateTravelerCode(FeltCodeCursor(artifact.input, rangeId = 1))
    } finally FeltReporter.recordTravelerGenerate(System.nanoTime - generationStart)

    val compilationStart = System.nanoTime
    try {
      FeltCompileEngine.generatedSourceToTravelerClassNames(artifact.key, artifact.tag, artifact.generatedSource) match {
        case Failure(t) =>
          throw t
        case Success(r) =>
          r.filter(!_.contains(runtimeSuffix)).head
      }
    } finally FeltReporter.recordTravelerCompile(System.nanoTime - compilationStart, artifact.generatedSource)

  }

}

