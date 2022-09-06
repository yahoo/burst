/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.literals.mutable

import org.burstsys.felt.model.mutables.valmap.FeltMutableValMap
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

/**
 * A literal value expression that reduces to a value map within the felt model.
 * <p/><b>NOTE:</b><i> these are designed to be very similar to the scalar value literals i.e. same basic handling of reads/writes/nulls. </i>
 * <h3>Semantics</h3>
 * <ol>
 * <li> '''Declaration:'''
 * Felt map literals can be declared as the type of an immutable variable (local, global, parameter, of xact)
 * <pre> '''val''' variable_name:'''map'''[value_type, value_type] = '''map'''(map_association*)</pre>
 * e.g.
 * <pre> '''val''' m1:'''map'''[string, long] = '''map'''("foo" -> 4, "bar" -> 5, "snark" -> null)</pre>
 * </li>
 * <li> '''Immutable:'''
 * <p/>map literals declarations must be a ''val'' i.e.
 * <pre>m1 = '''map'''("foo" -> 4, "bar" -> 0 ) // won't work</pre>
 * </li>
 * <li> '''Empty Maps'''
 * <p/>map literals can be declared to be empty in the initialization i.e.
 * <pre> '''val''' m1:'''map'''[string, long] = '''map'''() // ok </pre>
 * but since they cannot change size, they will remain empty.
 * </li>
 * <li> '''Initializer:'''
 * The declaration must include an initializer. The initializer must not have any null keys but it can have
 * null values. The size of the map is fixed by the number of entries in the initializer and cannot be changed.
 * </li>
 * <li> '''Write access in expressions:'''
 * <p/>Map keys can '''not''' be set during runtime, nor can keys be '''added'''.
 * <p/>Map values '''can''' be set during runtime e.g. <pre>m1("foo") = 5 // ok</pre>
 * Map values '''can''' be set to ''null'' e.g. <pre>m1("snark") = ''null'' // ok</pre>
 * <li> '''Read access in expressions:'''
 * <p/>Map values '''can''' be read during runtime e.g. <pre> some_variable = m1("foo") + m1("bar") // ok</pre>
 * The value of a ''null'' key is ''null''. e.g. <pre>m1(''null'') == ''null'' // is true </pre>
 * </li>
 * <li> '''Set Operations:'''
 * <p/>TBD</pre>
 * </ol>
 * <h3>Optimizations</h3>
 * <ol>
 * <li>maps keys are stored in sorted form support binary search comparisons</li>
 * <li>set operations between map literals and brio maps can be optimized to logN</li>
 * <li>All string map keys in expressions are converted to dictionary key's - and there will be fast lookups
 *   - making string keys as performant as other keys</li>
 * </ol>
 *
 * @see [[FeltMutableValMap]]
 */
trait FeltValMapLit extends FeltMutableLit[FeltAssociation] {

  final override val nodeName = "felt-val-map-lit"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TYPE INFERENCE
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def resolveTypes: this.type = {
    members.foreach(_.resolveTypes)

    val keyType = FeltType.combine(members.map(_.key.feltType).toIndexedSeq: _*)
    val valType = FeltType.combine(members.map(_.value.feltType).toIndexedSeq: _*)

    feltType = FeltType.valMap(valType.valueType, keyType.valueType)
    this
  }


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltValMapLit = new FeltValMapLit {
    sync(FeltValMapLit.this)
    final override val members: Array[FeltAssociation] = FeltValMapLit.this.members.map(_.reduceStatics.resolveTypes)
    final override val location: FeltLocation = FeltValMapLit.this.location
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // CODE GENERATION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def generateExpression(implicit cursor: FeltCodeCursor): FeltCode = {
    validate()

    // first set up scopes to contain all sub expressions
    val associationCursors = members.map(_ => cursor indentRight 1 scopeDown)

    val associationsCode = for (i <- members.indices) yield {
      val assocCursor = associationCursors(i)

      val association = members(i)

      val keyExpr = association.key
      val valueExpr = association.value

      s"""|${T(this)}
          |${I}var ${assocCursor.callScope.scopeNull}:Boolean = false; var ${assocCursor.callScope.scopeVal}:${association.feltType.valueTypeAsCode} = ${association.feltType.valueDefaultAsCode}; // $nodeName-DECL
          |${association.generateExpression}
          |""".stripMargin

    }

    s"""$associationsCode
       |${classOf[Map[_, _]].getName}(
       |
       |$I)
       |""".stripMargin
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"map(${members.map(_.normalizedSource).mkString(", ")})"

}
