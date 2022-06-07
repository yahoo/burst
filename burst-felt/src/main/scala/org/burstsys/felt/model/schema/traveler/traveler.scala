/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema

import org.burstsys.felt.compile.artifact.{FeltArtifact, FeltArtifactKey, FeltArtifactTag}
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.felt.model.sweep.splice.FeltSpliceGenerator
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I}
import org.burstsys.vitals.logging.VitalsLogger
import org.burstsys.vitals.uid.newBurstUid

import scala.language.postfixOps

/**
 * the [[FeltTraveler]] is what actualizes the travel through the tree structure
 * of a given [[org.burstsys.brio.model.schema.BrioSchema]] i.e.
 * they are ''code generated'' to instrument the traversal of a brio schema to support the [[FeltSweep]]
 * single pass scan algorithm. There can be more than one ''specialized'' e.g. lexicon or no lexicon
 * version of a felt schema per active Brio schema. This number is small as it is desirable to ''generated/compile/cache''
 * a rather small number of felt schemas for performance reasons. So while there is a sweep
 * ''generated/compile/cache'' for each unique analysis (and re used via parameterization), there are only few
 * felt schema per active brio schema.
 * */
package object traveler extends VitalsLogger {

  val runtimeSuffix = "_runtime"
  val lexiconSuffix = "_lexicon"

  /**
   * this allows traveler to be ''cached'' after generation/compilation (and reused)
   *
   * @param key
   * @param tag
   * @param input
   */
  final case
  class FeltTravelerArtifact(key: FeltArtifactKey, tag: FeltArtifactTag, input: FeltGlobal) extends FeltArtifact[FeltGlobal] {

    override val name: String = "traveler"

    lazy val lexiconEnabled: Boolean = input.lexicon.enabled

    lazy val travelerClassName: String = s"${newBurstUid}${if (lexiconEnabled) lexiconSuffix else ""}"

    lazy val runtimeClassName: String = s"$travelerClassName$runtimeSuffix"

    var generatedSource: String = _

  }

  case
  class FeltSearchMethod(runtimeClassName: String, methodName: String, generator: FeltSpliceGenerator) {

    private[this]
    var _code: FeltCode = _

    def methodCall: FeltCode = s"${methodName}($schemaRuntimeSym, $schemaSweepSym, $blobReaderSym)"

    def methodDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
      if (_code == null) {
        val body = generator(cursor indentRight)
        val parameters = s"""$schemaRuntimeSym:$runtimeClassName, $schemaSweepSym: $feltSweepClass, $blobReaderSym:$teslaReaderClass"""
        _code =
          s"""|
              |$I@inline final def $methodName($parameters): Unit = {$body
              |$I}""".stripMargin
      }
      _code
    }

    override def toString: String = methodCall

  }

}
