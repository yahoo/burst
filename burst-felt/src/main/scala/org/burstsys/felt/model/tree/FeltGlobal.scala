/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.binding.FeltBinding
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.FeltArtifactTracker
import org.burstsys.felt.model.reference.FeltRefLinker
import org.burstsys.felt.model.reference.names.{FeltNameSpace, FeltNameSpaceGlobal}
import org.burstsys.felt.model.schema.FeltSchema
import org.burstsys.felt.model.sweep.lexicon.FeltLexicon
import org.burstsys.vitals.uid.{VitalsUid, newBurstUid}

import java.util.concurrent.atomic.AtomicLong

/**
 * all the globals (metadata common to all nodes in the tree)
 * This is a good place to put things that you want all over the place. Its attached to
 * every FeltNode as well as other places
 */
trait FeltGlobal extends AnyRef with FeltNameSpaceGlobal {

  /**
   * globally unique UID for this FELT tree. This is also the generated class name base.
   *
   * @return
   */
  def treeGuid: VitalsUid

  /**
   * the source text for this AST
   *
   * @return
   */
  def source: String

  /**
   * the brio schema
   *
   * @return
   */
  def brioSchema: BrioSchema

  /**
   *
   * @return
   */
  def rootNameSpace: FeltNameSpace

  def rootNameSpace_=(a: FeltNameSpace): Unit

  /**
   *
   * @return
   */
  def analysis: FeltAnalysisDecl

  def analysis_=(a: FeltAnalysisDecl): Unit

  def analysisName: String = analysis.analysisName

  final
  def feltSchema: FeltSchema = analysis.schemaDecl.feltSchema

  /**
   * the class name of the generated FeltSchema
   *
   * @return
   */
  def travelerClassName: String

  def travelerClassName_=(name: String): Unit

  /**
   * hook up a schema other than the default one
   *
   * @param schema
   */
  def bind(schema: BrioSchema): Unit

  /**
   * the reference linker
   *
   * @return
   */
  def linker: FeltRefLinker

  /**
   *
   * @return
   */
  def binding: FeltBinding

  /**
   * collectors keep track of actual use of
   * generated artifacts to make sure
   * we can elide them if not used.
   *
   * @return
   */
  def artifactTracker: FeltArtifactTracker

  /**
   * the term lexicon
   *
   * @return
   */
  def lexicon: FeltLexicon

  /**
   * various FELT options
   *
   * @return
   */
  def features: FeltFeatures

  /**
   *
   * @return
   */
  def newTreeId: Long

}

object FeltGlobal {

  def apply(
             source: String = null, brioSchema: BrioSchema = null, binding: FeltBinding = null,
             travelerClassName: String = null
           ): FeltGlobal =
    FeltGlobalContext(source = source, brioSchema = brioSchema, binding = binding, travelerClassName)

}

private final case
class FeltGlobalContext(var source: String, var brioSchema: BrioSchema, var binding: FeltBinding, var travelerClassName: String)
  extends FeltGlobal with FeltFeatures {

  override def toString: VitalsUid =
    s"""|
        |FELT_GLOBAL:
        |  schema=${brioSchema.name}, travelerClassName=$travelerClassName
        |  lexicon_enabled=${lexicon.enabled} $printFeatures
        |  source=
        |$source""".stripMargin

  override
  val lexicon: FeltLexicon = FeltLexicon()

  override
  val linker: FeltRefLinker = FeltRefLinker(this)

  override
  val features: FeltFeatures = this

  private[this]
  var _rootNameSpace: FeltNameSpace = _

  private[this]
  var _analysis: FeltAnalysisDecl = _

  override var ctrlVerbs: Boolean = false

  private[this] final val _idGenerator = new AtomicLong(0)

  override def bind(schema: BrioSchema): Unit = this.brioSchema = schema

  override def newTreeId: Long = _idGenerator.incrementAndGet()

  override val treeGuid: VitalsUid = newBurstUid

  override def rootNameSpace: FeltNameSpace = _rootNameSpace

  override def rootNameSpace_=(a: FeltNameSpace): Unit = _rootNameSpace = a

  override def analysis: FeltAnalysisDecl = _analysis

  override def analysis_=(a: FeltAnalysisDecl): Unit = {
    _analysis = a
  }

  override val artifactTracker: FeltArtifactTracker = FeltArtifactTracker()
}
