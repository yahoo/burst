/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.types.FeltType

import scala.language.postfixOps

/**
 * =Expressions=
 * This package contains the types that support the construction of ''expressions'' within the Felt analysis tree.
 * Expressions generally are  used to initialize state and to execute behavior statements within visit actions.
 * ==expression functions==
 * Expressions can be thought of as a ''function'' with a signature consisting of a ''range'' and a ''domain''. The domain
 * or input consists of zero or more other expressions. The range or output consists of zero or one value.
 * ==complex expressions==
 * Since expressions can have other expressions as their domain, and in turn have their range become part of the domain
 * of another expression, we have what is called a '''complex expression'''. The composition of expressions with other
 * expressions is a central part of expression code generation.
 * ==expression statements==
 * A simple or complex expression can be placed into a Felt tree in such a way that it becomes a ''statement'' that
 * can be executed. For instance in a [[org.burstsys.felt.model.visits.decl.FeltActionDecl]] (within a
 * [[org.burstsys.felt.model.visits.decl.FeltVisitDecl]] there is an [[FeltExprBlock]] that is a
 * sequence of one or more statements that are executed (in order) at a certain point in the scan/traversal.
 * The difference between an expression and a statement is that an expression has to be positioned into a
 * statement context in order to have a place and time to have its semantics 'executed'.
 * ==value and nullity==
 * Because both the Brio data model that Felt relied on, and Felt itself is quite strict
 * in their consistent model for (relentless tracking of) ''nullity'' i.e. the known or unknown
 * condition of any value, all expressions and their domain and their range always include these
 * two somewhat separate concerns: the value and the nullity of that value i.e. is that value known or unknown. This
 * complicates the way that functions are composed in the code generated runtime.
 * ==literals==
 * There is a special type of expression called a ''literal''. These constant values have special properties e.g.
 * any complex expression that can be ''reduced'' to a literal can be simplified at compile/code generation time.
 * ==value and side effects==
 * Expressions can be used to operate on their domain and produce a range that is some reasoning on their domain and/or
 * they can produce 'side effects' i.e. the execution of the function will have some impact on the semantics of the
 * Felt analysis that is not represented in the range e.g. functions can directly effect the state of
 * [[org.burstsys.felt.model.collectors.runtime.FeltCollector]] instances. Functions can also return values or have
 * side effects that directly ''read'' state that is outside the domain e.g. again functions can ''read''  the
 * state of [[org.burstsys.felt.model.collectors.runtime.FeltCollector]] instances and use that state to impact
 * the semantics of that function. Generally we don't worry a lot about the difference between pure state functions
 * and ''side effect'' functions but its worth noting them because they may or may not impact optimization techniques
 * for code generation.
 * ==expression types==
 * <ul>
 * <li>'''function calls'''  {{{func(domain) -> range }}}
 * there are many functions built into Felt that can be executed by a ''call''
 * </li>
 * <li>'''assignment'''  {{{expr1 = expr2 }}} Some expressions allow for ''assignment''
 * from another expression's range. This assignment operation
 * is syntactic sugar for the using that expression in a specialized form of domain</li>
 * <li>'''boolean'''  {{{expr -> boolean }}} a category of expressions that have a true or false range</li>
 * <li>'''cast ''' {{{cast(type1) as type2 }}} a category of expression that takes a value
 * of one type in the domain and has a range
 * consisting of the same value as another type. </li>
 * <li>'''compare''' {{{cmp(value1, value2) -> boolean }}} a category of boolean expression that takes values
 * in its domain and produces a true or false (or null)
 * return that is some sort of comparison of those input values.</li>
 * <li>'''flow'''  {{{flow(boolean_expr...) -> set(statement) }}}
 * a category of operation with very specialized syntax and grammar, that controls what expression statements
 * are executed at runtime based on boolean domain inputs.</li>
 * <li>'''inclusion'''  {{{inclusion_test(value_expr) -> boolean }}}</li>
 * <li>'''math'''  {{{ value OP value -> value  OP value -> value }}}</li>
 * <li>'''time and date'''  </li>
 * </ul>
 *
 * @see [[org.burstsys.felt.model.expressions.FeltExpression]]
 * @see [[org.burstsys.felt.model.literals.FeltLiteral]]
 * @see [[org.burstsys.felt.model.expressions.assign.FeltAssignExpr]]
 * @see [[org.burstsys.felt.model.expressions.bool.FeltBoolExpr]]
 * @see [[org.burstsys.felt.model.expressions.cast.FeltCastExpr]]
 * @see [[org.burstsys.felt.model.expressions.cmp.FeltCmpBoolExpr]]
 * @see [[org.burstsys.felt.model.expressions.flow.FeltFlowExpr]]
 * @see [[org.burstsys.felt.model.expressions.assign.FeltAssignExpr]]
 * @see [[org.burstsys.felt.model.expressions.math.FeltMathExpr]]
 * @see [[org.burstsys.felt.model.expressions.time.FeltDatetimeExpr]]
 * @see [[org.burstsys.felt.model.expressions.inclusion.FeltInclusionExpr]]
 */
package object expressions {

  /**
   * __EXPRESSION CALL STRUCTURE__
   * {{{
   * when generating code from an expression tree, there is a functional 'caller function' calling 'callee function' pattern.
   * The parent tree node (caller) sets up a appropriately typed return (range) nullity and value holder and the
   * child tree node (callee) returns its range to that holder. The parent is visible to the child, but the child is not
   * visible to the parent. This visibility rule may be violated in implementation, but must remain semantically defensible.
   * }}}
   */
  final val EXPRESSION_CALL_STRUCTURE = true

  /**
   * generate a result (domain) __holder__ for an expression function...
   * this creates a `nullity` boolean var and a `value` var pair for all result possibilities
   * __NOTE:__ all expression tree parents (callers) have to provide a result declaration
   * for their child expressions (callees) unless those child expressions have a __Unit__ (no)
   * type
   *
   * @param nodeName added info for generated code comment
   * @param cursor
   * @return
   */
  final
  def callerRangeDeclare(feltType: FeltType, nodeName: String)(implicit cursor: FeltCodeCursor): FeltCode = {
    if (feltType.unitTypeType) return FeltNoCode
    val scalaType = feltType.valueTypeAsCode
    val scalaDefault = feltType.valueDefaultAsCode
    s"${I}var ${cursor.callScope.scopeNull}:Boolean = false; var ${cursor.callScope.scopeVal}:${scalaType} = ${scalaDefault}; // $nodeName-caller-range-decl"
  }

  /**
   * setup an expression (result declaration followed by expression execution) to be used in subsequent expressions
   *
   * @param expr
   * @param nodeName
   * @param cursor
   * @return
   */
  final
  def expressionEvaluate(expr: FeltExpression, nodeName: String)(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${callerRangeDeclare(expr.feltType, s"$nodeName")}
        |$I{${expr.generateExpression(cursor indentRight)}
        |$I}""".stripMargin
  }

  final
  def calleeRangeReturn(caller: FeltCodeCursor, callee: FeltCodeCursor, nodeName: String)(implicit cursor: FeltCodeCursor): FeltCode = {
    s"${I}if ( ${callee.callScope.scopeNull} ) ${caller.callScope.scopeNull} = true; else { ${caller.callScope.scopeNull} = false;${caller.callScope.scopeVal} = ${callee.callScope.scopeVal}; } // $nodeName-callee-range-return"
  }

}
