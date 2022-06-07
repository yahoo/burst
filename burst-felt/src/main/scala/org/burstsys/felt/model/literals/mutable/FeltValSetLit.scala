/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.mutable

import org.burstsys.felt.model.expressions.FeltExpression
import org.burstsys.felt.model.mutables.valset.FeltMutableValSet
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types.FeltType
import org.burstsys.felt.model.variables.global.FeltGlobVarDecl
import org.burstsys.felt.model.variables.local.FeltLocVarDecl
import org.burstsys.felt.model.variables.parameter.FeltParamDecl

import scala.language.postfixOps

/**
 * A literal value expression that reduces to a value vector within the felt model.
 * <p/><b>NOTE:</b><i> these are designed to be very similar to the scalar value literals i.e. same basic handling of reads/writes/nulls. </i>
 * <h4>Felt Vector Declaration</h4>
 * <ol>
 * <li> '''Declaration:'''
 * Felt vector literals can be declared as the type of an immutable variable (local, global, parameter, of xact)
 * [[FeltGlobVarDecl]],
 * ''local'' variable [[FeltLocVarDecl]] or parameter
 * [[FeltParamDecl]].
 * <pre> '''val''' variable_name:'''vector'''[value_type] = '''vector'''(value_expr*)</pre>
 * e.g.
 * <pre> '''val''' ''v1'':'''vector'''[long] = '''vector'''(0 , 3 , ''null'')</pre>
 * </li>
 * <li> '''Immutable:'''
 * <p/>vector literals declarations must be a ''val'' i.e.
 * <pre>v1 = '''vector'''( 4,  0 ) // won't work</pre>
 * you cannot reassign them to another vector
 * </li>
 * <li> '''Initializer:'''
 * <p/>The declaration must include an initializer. The initializer can have
 * null values. The size of the vector is fixed by the number of entries in the initializer and cannot be changed.
 * </li>
 * <li> '''Empty Vectors'''
 * <p/>vector literals can be declared to be empty in the initialization i.e.
 * <pre> '''val''' ''v1'': '''vector'''[long] = '''vector'''() // ok </pre>
 * but since they cannot change size, they will remain empty.
 * </li>
 * <li> '''Write access in expressions:'''
 * <p/>vector values can be set via positional indices during runtime e.g. <pre>v1(3) = 5 // ok</pre>
 * Vector values can be set to ''null'' via positional indices at runtime e.g. <pre>v1(0) = ''null'' // ok</pre>
 * <li> '''Read access in expressions:'''
 * <p/>Vector values can be read via positional indices during runtime e.g. <pre> some_variable = v1(4) + v1(3) // ok</pre>
 * The value of a ''null'' position is ''null''. e.g. <pre>v1(''null'') == ''null'' // is true</pre>
 * </li>
 * <li> '''Set Operations:'''
 * <p/>TBD</pre>
 * </ol>
 * <h3>Optimizations</h3>
 * <ol>
 * <li>set operations between vector literals and brio value vectors can be optimized to logN (at some pt if valuable)</li>
 * <li>All string vector values in expressions are converted to dictionary key's - and there will be fast lookups
 *   - making string values as performant as other values</li>
 * </ol>
 *
 * @see [[FeltMutableValSet]]
 */
trait FeltValSetLit extends FeltMutableLit[FeltExpression] {

  final override val nodeName = "felt-val-set-lit"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    members.foreach(_.resolveTypes)
    val vectorMemberType = FeltType.combine(members.map(_.feltType): _*)
    feltType = FeltType.valSet(vectorMemberType.valueType)
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltValSetLit = new FeltValSetLit {
    sync(FeltValSetLit.this)
    final override val members: Array[FeltExpression] = FeltValSetLit.this.members.map(_.reduceStatics.resolveTypes)
    final override val location: FeltLocation = FeltValSetLit.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    validate()

    // first set up scopes to contain all sub expressions
    val valuesCursors = members.map(_ => cursor indentRight 1 scopeDown)

    val valuesCode = for (i <- members.indices) yield {
      val valueCursor = valuesCursors(i)

      val valueExpr = members(i)

      val valueCode = valueExpr.generateExpression

      s"""|
          |${T(this)}
          |${I}var ${valueCursor.callScope.scopeNull}:Boolean = false; var ${valueCursor.callScope.scopeVal}:${valueExpr.feltType.valueTypeAsCode} = ${valueExpr.feltType.valueDefaultAsCode}; // $nodeName-DECL
          |${valueExpr.generateExpression}
          |""".stripMargin

    }

    s"""|
        |$valuesCode
        |${classOf[Array[_]].getName}()
        |""".stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = s"set(${members.map(_.normalizedSource).mkString(", ")})"

}
