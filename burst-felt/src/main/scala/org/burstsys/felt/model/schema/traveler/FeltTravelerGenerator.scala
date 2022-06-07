/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema.traveler

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.felt.model.control.generate.FeltCtrlTravGen
import org.burstsys.felt.model.lattice.FeltLatTravGen
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.splice._
import org.burstsys.felt.model.sweep.symbols._
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.vitals.strings._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

trait FeltTravelerGenerator {

  def generateTravelerCode(implicit cursor: FeltCodeCursor): FeltCode

  def ++=(methods: List[FeltSearchMethod]): Unit

  def +=(method: FeltSearchMethod): Unit

  def searchMethods: List[FeltSearchMethod]

  def travelerClassName: String

  def runtimeClassName: String

  def brioSchema: BrioSchema

}

object FeltTravelerGenerator {

  def apply(travelerClassName: String, runtimeClassName: String, brioSchema: BrioSchema): FeltTravelerGenerator =
    FeltTravelerGeneratorContext(travelerClassName: String, runtimeClassName: String, brioSchema: BrioSchema)

}

/**
 * generate code to implement a [[FeltTraveler]]
 * for a specific brio schema
 */
private final case
class FeltTravelerGeneratorContext(travelerClassName: String, runtimeClassName: String, val brioSchema: BrioSchema)
  extends FeltTravelerGenerator {

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  /////////////////////////////////////////////////////////////////////////////////////////////////////
  private[this]
  val _controls = FeltCtrlTravGen(this)

  private[this]
  val _lattice = FeltLatTravGen(this)

  private[this]
  val _searchMethods = new ArrayBuffer[FeltSearchMethod]

  /////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////

  override def searchMethods: List[FeltSearchMethod] = _searchMethods.toList

  override def +=(method: FeltSearchMethod): Unit = _searchMethods += method

  override
  def ++=(methods: List[FeltSearchMethod]): Unit = _searchMethods ++= methods

  override
  def generateTravelerCode(implicit cursor: FeltCodeCursor): FeltCode = {
    val lexiconTag = if (cursor.global.lexicon.enabled) "WITH_LEXICON" else "WITHOUT_LEXICON"
    s"""|
        |${C(s"generated felt 'traveler' for brio schema '${brioSchema.name}' $lexiconTag)")}
        |$generateTravelerRuntime
        |${I}class $travelerClassName extends $feltTravelerClass[$runtimeClassName] {
        |${I2}final def travelerClassName:String = "$travelerClassName";
        |${I2}final def runtimeClassName:String = "$runtimeClassName";
        |${I2}final val $brioSchemaSym = $brioSchemaClass("${brioSchema.name}");
        |${generateSchematicReferences(cursor indentRight 1)}${body(brioSchema)(cursor indentRight 1)}
        |${generateLatticeVarInitializations(brioSchema, runtimeClassName)(cursor indentRight 1)}
        |${_controls.generateControlSchema(cursor indentRight)}
        |${generateSearchMethods(cursor indentRight 1)}
        |$I}""".stripMargin.replaceAllLiterally("OR", "|").stripEmptyLines
  }

  private
  def body(brioSchema: BrioSchema)(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C(s"called per brio blob traversed with analysis-specific parallel 'sweep' (reentrant) and 'runtime' (one per thread)")}
        |$I@inline final def apply($schemaRuntimeSym:$runtimeClassName, $schemaSweepSym: $feltSweepClass): Unit = {
        |${I2}val reader = runtime.reader;
        |${generateLatticeVarInitializeCall(cursor indentRight 1)}
        |${I2}val rootPathKey = $brioSchemaSym.rootNode.pathKey;
        |$I2$schemaSweepSym.rootSplice($schemaRuntimeSym, rootPathKey, ${FeltTraverseCommencePlace.key}); // $FeltTraverseCommencePlace
        |${_lattice.generateLatticeSchemaRoot(brioSchema.rootNode)(cursor indentRight 1)}
        |$I2$schemaSweepSym.rootSplice($schemaRuntimeSym, rootPathKey, ${FeltTraverseCompletePlace.key}); // $FeltTraverseCompletePlace
        |$I}""".stripMargin
  }

  private
  def generateSchematicReferences(implicit cursor: FeltCodeCursor): FeltCode = {
    def schematicCode(implicit cursor: FeltCodeCursor): FeltCode = (cursor.schema.brioSchema.structures map {
      s =>
        def versionCode(implicit cursor: FeltCodeCursor): FeltCode = (for (version <- 1 to cursor.schema.brioSchema.versionCount) yield
          s"""|
              |${I}final val ${schematic(s.structureTypeName, version)}:$brioSchematicClass = $brioSchemaSym.schematic(${s.structureTypeKey}, $version);""".stripMargin
          ).mkString

        versionCode
    }).noNulls.mkString

    s"""|
        |${C("schematic references")}
        |$schematicCode""".stripMargin
  }

  private
  def generateSearchMethods(implicit cursor: FeltCodeCursor): FeltCode = {
    // we need to generate our methods at this cursor point, but each generate will add to the list we need to generate
    val code = new ArrayBuffer[String]
    // make sure you don't generate the same method twice
    val seenMethods = new mutable.HashSet[String]
    while (_searchMethods.nonEmpty) {
      // take a search method off the list
      val method = _searchMethods.remove(0)
      if (!seenMethods.contains(method.methodName)) {
        seenMethods += method.methodName
        // generate
        code += method.methodDeclaration
        // this may generate more search methods so need to check
      }
    }
    // all methods have been generated
    s"""|
        |${C(s"traveler search methods")}${code.stringify}""".stripMargin
  }

  /**
   * code generate appropriate [[FeltRuntime]] subtype that is the base class for per scan traversal
   * runtime data management
   *
   * @return
   */
  private
  def generateTravelerRuntime(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${C("traveler specific abstract base runtime class ")}
        |${I}trait $runtimeClassName extends $feltRuntimeClass {
        |${generateLatticeVarDeclarations(cursor indentRight 1)}
        |$I}""".stripMargin
  }

}


