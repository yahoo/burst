/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.planning.lanes

import org.burstsys.eql._
import org.burstsys.eql.actions.{ControlExpression, ControlTestTemporary}
import org.burstsys.eql.generators.{ActionPhase, DeclarationScope}
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.DeclarationScope.DeclarationScope
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.common.DataType
import org.burstsys.motif.motif.tree.expression.Expression
import org.burstsys.motif.motif.tree.logical._
import org.burstsys.motif.motif.tree.logical.context.UnaryBooleanExpressionContext
import org.burstsys.motif.motif.tree.values.ValueExpression
import org.burstsys.vitals.errors.VitalsException

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

object LaneControl {

  def apply(): LaneControl = new LaneControlImpl()

  /**
    * Place the information for conditional enforcement at each visit.
    *
    * The basic control actions should have already been placed by a call to [[extractAndControls()]].
    * This call will place the temporary initializations, and additional tests at lower and higher
    * visits to enforce nullity
    *
    * Walking the visit tree in DFS order for each node:
    *   # get the results from all direct ancestors
    *   # make a summary result for each result living here
    *   # collect the results for each basic control a this node
    *   # collect the summary results for direct descendants
    *   # make a pre action that
    *      * initializes all the collected summary results
    *      * evaluates and sets the results
    *      * puts a guard before code that and's the ancestor results and this visit's results
    *   # make a post or situ action that
    *      * put a guard around code and an assignment of this nodes summary result to true with a conjunction of
    *         this visit results, the direct ancestor results and the direct descents' summary result
    *
    */
  def placeLaneControls(lane: LaneName, isAbortableAllowed: Boolean)(visits: Visits)(implicit global: GlobalContext): Unit = {
    // First identify the control checks done in the pre-step
    visits.preWalkVisits[ControlTestTemporary](lane) { (path, steps, parent) =>
      if (steps.isEmpty)
        // nothing is here so just pass the control down
        parent
      else {
        val s = steps.get
        if (s.control.controls(ActionPhase.Pre).nonEmpty ||
          (parent.nonEmpty && s.actions.nonEmpty) ||
          (parent.nonEmpty && s.doesDimensionWrite)) {
          // this node has definitive early predicates to test
          // add the parent control variable to this test
          if (parent.isDefined) {
            s.control.assignControl().addControlTest(ActionPhase.Pre, parent.get)
          }

          //TODO expand this when abort other than the root level are supported
          val isLaneAbortable: Boolean = path.isRoot && lane == RESULT && isAbortableAllowed
          /* || {
            val parentLanes = if (visits.visitMap.contains(path.getParentStructure))
              visits.visitMap(path.getParentStructure).laneMap.keys
            else
              Iterable.empty
            parentLanes.isEmpty || (parentLanes.size == 1 && parentLanes.head == lane)
          }
          */

          // pass our control variable to children for checking
          s.control.assignControl(isAbortable = isLaneAbortable).getVisitControlTest
        } else
        // nothing to check here
          parent
      }
    }

    // Next the checks needed at the post-step
    val finalControl =
    visits.postWalkVisits[ControlTestTemporary] (lane) { (path, steps, children) =>
      def addControl(s: LaneActions): Option[ControlTestTemporary] = {
        val isLaneAbortable: Boolean = path.isRoot && lane == RESULT && isAbortableAllowed
        children.foreach(s.control.addControlTest(ActionPhase.Post, _))
        children.foreach(_.needsSummary = true)
        s.control.assignControl(isAbortable = isLaneAbortable).getVisitControlTest
      }

      if (steps.isEmpty) {
        if (children.length > 1
          || (children.length == 1 && path.isRoot)
        ) {
          addControl(visits.getOrCreate(path, lane))
        } else if (children.length == 1) {
          // our single child might need checking, but we can have our ancestors do it
          Some(children.head)
        } else None
      } else {
        val s = steps.get
        if (
          s.control.hasControls ||                                          // we have our own control
            children.length > 1 ||                                       // we need to combine child results
            // (children.length == 1 && path == visits.schema.getRootFieldName) ||  // we are to root with a child
            (s.actions.exists(_.phase() == ActionPhase.Post) && children.nonEmpty) ||  // we have controlled delayed actions
            (s.doesDimensionWrite && children.nonEmpty)              // we have controlled delayed dimension write
        ) {
          addControl(s)
        } else if (children.length == 1) {
          // our single child might need checking, but we can have our ancestors do it
          Some(children.head)
        } else None
      }
    }
    if (lane == RESULT && finalControl.nonEmpty)
      finalControl.get.needsSummary = true
  }

  /**
    * Given a boolean expression break it into parts that are logical conjunctions.
    *
    * @param expression the expression
    * @return an array of expressions that satisfy the original expression when AND'd together
    */
  def extractAndControls(expression: Expression): Array[ControlExpression] =
    extractAndControls(expression, controls= null).toArray

  /**
    * Given a boolean expression break it into parts that are logical conjunctions.
    *
    * @param expression the boolean expression
    * @param controls optional list of existing controls
    * @param invert invert the sense of disjunction and conjunction because the this expression is surrounded by a not
    * @return
    */
  private def extractAndControls(expression: Expression, controls: ArrayBuffer[ControlExpression] = null, invert: Boolean = false): ArrayBuffer[ControlExpression] = {
    val ctls: ArrayBuffer[ControlExpression] = if (controls != null) controls else ArrayBuffer[ControlExpression]()
    if (expression == null)
      return ctls
    expression match {
      case and: BinaryBooleanExpression if and.getOp == BinaryBooleanOperatorType.AND =>
        val l = if (invert) negateNode(and.getLeft) else and.getLeft
        val r = if (invert) negateNode(and.getRight) else and.getRight
        extractAndControls(l, ctls, invert)
        extractAndControls(r, ctls, invert)
      case not: UnaryBooleanExpression if not.getOp == UnaryBooleanOperatorType.NOT =>
        not.getExpr match {
          case or: BinaryBooleanExpression if or.getOp == BinaryBooleanOperatorType.OR =>
            // or expressions can be inverted
            extractOrControls(or, ctls, invert = !invert)
          case x =>
            // nothing else is worth it for now
            ctls += ControlExpression(not)
        }
      case b: BooleanExpression =>
        // not a control we can break apart
        ctls += ControlExpression(b)
      case v: ValueExpression if v.getDtype == DataType.BOOLEAN =>
        // not a control we can break apart
        ctls += ControlExpression(v)
      case e =>
        // invalid expression in a where that should have parsed
        throw VitalsException(s"unexpected expression found in AND control extraction: '$e'")
    }
    ctls
  }

  private def extractOrControls(expression: Expression, controls: ArrayBuffer[ControlExpression], invert: Boolean = false): ArrayBuffer[ControlExpression] = {
    if (expression == null)
      return controls
    expression match {
      case or: BinaryBooleanExpression if or.getOp == BinaryBooleanOperatorType.OR =>
        val l = if (invert) negateNode(or.getLeft) else or.getLeft
        val r = if (invert) negateNode(or.getRight) else or.getRight
        extractOrControls(l, controls, invert)
        extractOrControls(r, controls, invert)
      case not: UnaryBooleanExpression if not.getOp == UnaryBooleanOperatorType.NOT =>
        // negation so switch to looking for ands and invert them
        not.getExpr match {
          case and: BinaryBooleanExpression if and.getOp == BinaryBooleanOperatorType.AND =>
            // and expressions can be inverted
            extractAndControls(and, controls, invert = !invert)
          case x =>
            // nothing else is worth it for now
            controls += ControlExpression(not)
        }
      case b: BooleanExpression =>
        // not a control we can break apart
        controls += ControlExpression(b)
      case v: ValueExpression if v.getDtype == DataType.BOOLEAN =>
        // not a control we can break apart
        controls += ControlExpression(v)
      case e =>
        // invalid expression in a where that should have parsed
        throw VitalsException(s"unexpected expression found in OR control extraction: '$e'")
    }

    controls
  }

  private def negateNode(expression: BooleanExpression): BooleanExpression = {
    expression match {
      case not: UnaryBooleanExpression if not.getOp == UnaryBooleanOperatorType.NOT =>
        // just strip the original not
        not.getExpr
      case b: UnaryBooleanExpressionContext =>
        new UnaryBooleanExpressionContext(b.getGlobal, b.getLocation, UnaryBooleanOperatorType.NOT, b)
      case _ =>
        throw VitalsException(s"unexpected boolean expression type: $expression")
    }
  }

}

/**
  * A lane control encapsulates all the control structures that determine if actions in a lane should be executed.
  * Control of a lane are influenced by *ControlExpressions* extracted from the where clauses of expressions and
  * *ControlTests* which are the results of external controls computed outside this visit.
  *
  * The two outside controls are the result of the parent's controls and the results of the children's controls.
  * Parent tests are computed before starting the visit and can be evaluated early.  Children tests are available only
  * in the post phase of a visit and must be delayed..
  *
  */
trait LaneControl {
  def isAbortable: Boolean

  def controls(phase: ActionPhase): Iterator[ControlExpression]
  def tests(phase: ActionPhase): Iterator[ControlTestTemporary]

  def hasControls:Boolean

  def assignControl(scope: DeclarationScope = DeclarationScope.Analysis, isAbortable: Boolean = false)(implicit global: GlobalContext): this.type

  def addControlExpression(step: ControlExpression): this.type

  def addControlTest(phase: ActionPhase, controlTest: ControlTestTemporary): this.type

  def getVisitControlTest: Option[ControlTestTemporary]
}

class LaneControlImpl extends LaneControl {
  var visitControlTest: Option[ControlTestTemporary] = None
  var isAbortable:Boolean = false

  val extraControlTests:  Map[ActionPhase, mutable.Queue[ControlTestTemporary]] = Map (
    ActionPhase.Before -> mutable.Queue[ControlTestTemporary](),
    ActionPhase.Pre -> mutable.Queue[ControlTestTemporary](),
    ActionPhase.Post -> mutable.Queue[ControlTestTemporary](),
    ActionPhase.After -> mutable.Queue[ControlTestTemporary]()
  )

  val controlExpressions: Map[ActionPhase, mutable.Queue[ControlExpression]] = Map (
    ActionPhase.Before -> mutable.Queue[ControlExpression](),
    ActionPhase.Pre -> mutable.Queue[ControlExpression](),
    ActionPhase.Post -> mutable.Queue[ControlExpression](),
    ActionPhase.After -> mutable.Queue[ControlExpression]()
  )

  def controls(phase: ActionPhase): Iterator[ControlExpression] = controlExpressions(phase).iterator
  def tests(phase: ActionPhase): Iterator[ControlTestTemporary] = extraControlTests(phase).iterator

  override def hasControls: Boolean = {
    controlExpressions.values.exists(_ nonEmpty)
  }

  override def addControlExpression(control: ControlExpression): this.type = {
    controlExpressions(control.phase()).enqueue(control)
    this
  }

  def assignControl(scope: DeclarationScope, isAbortable:Boolean=false)(implicit global: GlobalContext): this.type = {
    if (visitControlTest.isEmpty) {
      visitControlTest = Some(ControlTestTemporary(global.temporaryName, scope=scope))
      this.isAbortable = isAbortable
    }
    this
  }

  def addControlTest(phase: ActionPhase, controlTest: ControlTestTemporary): this.type = {
    extraControlTests(phase).enqueue(controlTest)
    this
  }

  def getVisitControlTest: Option[ControlTestTemporary] = visitControlTest
}
