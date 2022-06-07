/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.splice

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.brio.types.BrioPath.BrioPathName
import org.burstsys.felt.model.expressions.callerRangeDeclare
import org.burstsys.felt.model.sweep.symbols.{feltRuntimeClass, schemaRuntimeSym, sweepRuntimeSym}
import org.burstsys.felt.model.tree.code.{C, FeltCode, FeltCodeCursor, FeltNoCode, I}
import org.burstsys.felt.model.tree.{FeltGlobal, FeltLocation}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.vitals.strings.VitalsString

/**
 * a splice is some sort of Felt construct that can be generated into a splice method. A splice method
 * becomes a code generated method on the [[org.burstsys.felt.model.sweep.FeltSweep]] and is called somewhere in
 * other Felt code. This is the based (abstract) trait/class there are multiple concrete implementations based on
 * how you want to specify the code to be generated.
 *
 * @see [[org.burstsys.felt.model.sweep.splice.FeltExprSplice]], [[org.burstsys.felt.model.sweep.splice.FeltGenSplice]]
 */
trait FeltSplice extends Any {

  /**
   * the global for this Felt Tree
   *
   * @return
   */
  def global: FeltGlobal

  /**
   * source location where this slices is related to
   *
   * @return
   */
  def location: FeltLocation

  /**
   * unique name for this splice
   *
   * @return
   */
  def spliceName: String

  /**
   * this is combination of {spliceName, pathName, and placement}
   *
   * @return
   */
  def spliceTag: String

  /**
   * the brio object tree ''path'' identifier
   *
   * @return
   */
  def pathName: BrioPathName

  /**
   * the relationship ''placement'' that controls where in relationship management traversal code is generated
   *
   * @return
   */
  def placement: FeltPlacement

  /**
   * if two splices are being called at the same traversal pt/placement, then do them in the order specified
   * here
   *
   * @return
   */
  def ordinal: Int

  /**
   * the code necessary to 'call' the splice method
   *
   * @param cursor
   * @return
   */
  def generateSpliceMethodCall(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * the code necessary for the body of the splice method
   *
   * @param cursor
   * @return
   */
  def generateSpliceMethodBody(implicit cursor: FeltCodeCursor): FeltCode

}

private[splice] abstract
class FeltSpliceContext(global: FeltGlobal, location: FeltLocation, spliceName: String, pathName: BrioPathName,
                        placement: FeltPlacement) extends FeltSplice {

  final override def toString: FeltCode = s"FeltSplice( '$spliceTag' )"

  final override val spliceTag: String =
    s"${spliceName}_${pathName}_${placement}".camelCaseToUnderscore

  protected var feltType: FeltType = _

  final val treeNode: BrioNode = global.feltSchema.nodeForPathName(pathName)

  protected
  def generateCode(implicit cursor: FeltCodeCursor): FeltCode

  final override
  def generateSpliceMethodCall(implicit cursor: FeltCodeCursor): FeltCode = {
    val body = generateSplice(cursor.modify(treeNode = treeNode) indentRight 1)
    if (body.isEmpty) return FeltNoCode
    s"""|
        |$I$methodName($schemaRuntimeSym, $sweepRuntimeSym, path, placement);""".stripMargin
  }

  final override
  def generateSpliceMethodBody(implicit cursor: FeltCodeCursor): FeltCode = {
    val treeGuid = global.treeGuid
    val signature =
      s"$methodName($schemaRuntimeSym: $feltRuntimeClass, $sweepRuntimeSym:$treeGuid, path: Int, placement: Int) : Unit"
    val body = generateSplice(cursor.modify(treeNode = treeNode) indentRight 1)
    if (body.isEmpty) return FeltNoCode
    s"""|
        |$I@inline
        |${I}def $signature = {$body
        |$I}""".stripMargin
  }

  private
  def generateSplice(implicit cursor: FeltCodeCursor): FeltCode = {

    val expressionCursor = cursor.modify(treeNode = treeNode) indentRight 1

    /**
     * splices since they are expressions (which are generally speaking functions), have possibility of a typed return (range)
     * The parent (caller) is always responsible for providing an appropriate place for the child (callee) to put those return values.
     * If the return type is Unit - then this is not necessary.
     */
    lazy val calleeDomain = if (feltType.unitTypeType) FeltNoCode else
      s"""|
          |${callerRangeDeclare(feltType, spliceTag)}""".stripMargin

    val callee = generateCode(expressionCursor)
    if (callee.isEmpty) return FeltNoCode
    s"""|
        |${C(s"$spliceTag header")}
        |${I}val reader = runtime.reader;$calleeDomain
        |$I{ // $spliceTag callee $callee
        |$I}""".stripMargin
  }

  private
  def methodName: String = s"splice_${spliceTag}"

}
