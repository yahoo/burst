/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.routes

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.actions.ControlTestTemporary
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.HydraVisitLanes.{PHASED, SITU, VisitType}
import org.burstsys.eql.generators._
import org.burstsys.eql.generators.hydra.frames.FrameSourceGenerator
import org.burstsys.eql.generators.hydra.routes.DFA.DFAState
import org.burstsys.eql.generators.hydra.routes.NDFA._
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.paths.{FunnelVisitPath, VisitPath}
import org.burstsys.eql.planning.funnels.{Funnel, TriggerStep}
import org.burstsys.eql.planning.lanes
import org.burstsys.eql.planning.lanes.LaneControl
import org.burstsys.eql.planning.visits.Visits
import org.burstsys.motif.common.DataType
import org.burstsys.motif.motif.tree.eql.funnels.Funnel.Tags
import org.burstsys.motif.motif.tree.eql.funnels._
import org.burstsys.motif.motif.tree.eql.funnels.context.FunnelMatchDefinitionStepIdContext
import org.burstsys.motif.motif.tree.values.ValueExpression
import org.burstsys.motif.paths.Path
import org.burstsys.motif.paths.funnels.FunnelPathBase
import org.burstsys.motif.paths.segments.SegmentPathBase
import org.burstsys.motif.schema.model.MotifSchema

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

final class Route(funnel: Funnel)(implicit context: GlobalContext) extends FrameSourceGenerator(funnel.getName) {

  implicit override def visits: Visits = funnel.getVisits

  private val dfa = DFA.expandToNodePerInput(DFA.toDFA(calculateNDFAGraph(funnel.getMatchDefinition)))

  // add state and additional behavior to funnel visits
  addRouteVisitControls(funnel.getVisits)
  context.addDeclaration(name, this)

  override def applyParameters(parameters: List[ValueExpression]): this.type = {
    funnel.getParameters.zip(parameters).foreach(x => x._1.value = x._2)
    this
  }

  override def filterPlacedControl(roots: Set[Path]): Boolean = roots.map{
    case fpb: FunnelPathBase =>
      // place it only if we use it as a source
      this.funnel.getSourceNames.contains(fpb.getFunnel.getName)
    case spb: SegmentPathBase =>
      // place it only if we use it as a source
      this.funnel.getSourceNames.contains(spb.getSegment.getName)
    case _ =>
      true
  }.reduce(_&&_)

  override def generateDeclarationSource(): CodeBlock = CodeBlock { implicit cb =>
    val startStates = dfa.startState.transitions.values.map(_.id).toArray // mutable.ArrayBuffer(: _*)
    s"route  {".source
    CodeBlock { implicit cb =>
      s"maxCompletePaths = ${funnel.getPathLimit}".source
      s"maxSteps = ${funnel.getStepLimit}".source
      if (funnel.getWithin > 0)
        s"maxPathTime = ${funnel.getWithin}".source
      s"graph {".source
      CodeBlock { implicit cb =>
        def makeStepDeclarationSource(state: DFAState): Unit = {
          if (state == dfa.startState) {
            // start state is a placeholder and isn't included in the route DFA
            return
          }
          val modifiers: mutable.ArrayBuffer[String] = new mutable.ArrayBuffer()
          if (startStates.contains(state.id)) {
            modifiers += "enter"
          }
          if (state.terminating) {
            modifiers += "complete"
          }
          if (state.tacit) {
            modifiers += "tacit"
          }
          s"${modifiers.mkString("",","," ")}${state.id} {".source
          CodeBlock { implicit cb =>
            for ((key, toState) <- state.transitions) {
              funnel.getSteps.find{ case ts: TriggerStep => ts.id == key } match {
                case Some(ts) =>
                  val triggeredStep = ts.asInstanceOf[TriggerStep]
                  val afterVal = triggeredStep.getAfterValue
                  val withinVal = triggeredStep.getWithinValue
                  s"to (${toState.id}, $afterVal, $withinVal)".source
                case None =>
                  throw new IllegalStateException(s"no step $key found in route")
              }
            }
          }.indent.source
          s"}".source
        }
        dfa.walkStates(s => makeStepDeclarationSource(s))
      }.indent.source
      s"}".source
    }.indent.source
    s"}".source
  }

  def addRouteVisitControls(visits: Visits)(implicit global: GlobalContext): Unit = {
    // isolate the transitions by step
    val transitions: mutable.ListBuffer[(StepTag, StateId)] = new ListBuffer()

    def extractAllTransitions(state: DFAState): Unit = {
      transitions ++= state.transitions.map { t => (t._1, t._2.id) }
    }

    dfa.walkStates(s => extractAllTransitions(s))

    val tagToStateIds: Map[StepTag, List[StateId]] = transitions.groupBy(_._1).map(e => e._1 -> e._2.map(_._2).distinct.toList)

    // transform planned trigger steps to ones with more complete information about the DFA
    visits.visitMap.foreach { e =>
      val (_, visitLanes) = e
      val laneControls: mutable.Set[LaneControl] = mutable.Set()
      val tagLaneControlMap = mutable.Map[StepTag,(LaneControl, RouteTriggerStep)]()
      val routeTempName = s"route_${funnel.getName}_control"
      visitLanes.laneMap.values.foreach { laneActions =>
        var foundTriggers = false
        laneActions.transform {
          case triggerStep: TriggerStep =>
            foundTriggers = true
            // start states are reevaluated at termination to kick off next path
            val triggerAction = new RouteTriggerStep(triggerStep, tagToStateIds.getOrElse(triggerStep.id, List.empty), routeTempName)
            if (dfa.startState.transitions.contains(triggerStep.id)) {
              tagLaneControlMap(triggerStep.id) = (laneActions.control,
                new RouteTriggerStep(triggerStep, List(dfa.startState.transitions(triggerStep.id).id), routeTempName))
            }
            triggerAction
          case x => x
        }
        if (foundTriggers) {
          laneControls.add(laneActions.control)
        }
      }
      // add cleanup controls to every phase
      Array(ActionPhase.Pre, ActionPhase.Post).filter(pv => laneControls.exists(lc => lc.controls(pv).nonEmpty)).foreach{p =>
        // add declarations for group trigger step testing
        val visitDFAProcessedTemp = new RouteVisitControlCleanup(p)
        val visitDFAControlTemp = new RouteVisitControl(routeTempName, visitDFAProcessedTemp, tagLaneControlMap, laneControls, p)
        visitDFAProcessedTemp.controlTestTemporary = visitDFAControlTemp
        visitLanes.addGenerator(lanes.INIT, new RouteVisitControlInit(visitDFAControlTemp, visitDFAProcessedTemp, p))
        visitLanes.addGenerator(lanes.RESULT, visitDFAControlTemp)
        visitLanes.addGenerator(lanes.CLEANUP, visitDFAProcessedTemp)
      }
    }
  }

  class RouteVisitControl(name: String,
                          controlProcessedTemporary: ControlTestTemporary,
                          startTagMap: mutable.Map[StepTag,(LaneControl, RouteTriggerStep)],
                          laneControls: mutable.Set[LaneControl],
                          phase: ActionPhase)
    extends ControlTestTemporary(name)
  {
    override lazy val tempVar: Var = Var(name, DeclarationScope.Visit, DataType.BOOLEAN)

    override def phase(): ActionPhase = phase

    override def generateSource()(implicit context: GlobalContext): CodeBlock = CodeBlock { implicit cb =>
      s"${controlProcessedTemporary.name}=true".source()
      s"if (!${tempVar.name}) {".source
      CodeBlock { implicit cb =>
        val controls = laneControls.filter(_.controls(phase).nonEmpty)
        if (!funnel.getTags.contains(Tags.LOOSE_MATCH.name)) {
          s"if (${controls.map{laneControl => laneControl.getVisitControlTest.get.tempVar.name}.mkString(" || ")}) {".source
          CodeBlock { implicit cb =>
            s"routeFsmEndPath(${funnel.getName})".source()
            s"routeScopeCommit(${funnel.getName})".source
            s"routeScopeStart(${funnel.getName})".source
          }.indent.source
          s"}".source
        }
        if (startTagMap.nonEmpty) {
          {
            val (control, state) = startTagMap.values.head
            s"if (${control.getVisitControlTest.get.tempVar.name}) {".source
            state.generateSource().indent.source
            s"}".source
          }
          for (e <- startTagMap.values.tail) {
            val (control, state) = e
            s"else if (${control.getVisitControlTest.get.tempVar.name}) {".source
            state.generateSource().indent.source
            s"}".source
          }
        }
      }.indent.source
      s"}".source
      s"routeScopeCommit(${funnel.getName})".source
    }
  }

  class RouteVisitControlInit(controlTestTemporary: ControlTestTemporary, controlProcessedTemporary: ControlTestTemporary, phase: ActionPhase)
    extends ActionSourceGenerator
  {
    override def phase(): ActionPhase = phase

    override def generateSource()(implicit context: GlobalContext): CodeBlock =  CodeBlock { implicit cb =>
      s"${controlTestTemporary.name}=false".source
      s"${controlProcessedTemporary.name}=false".source
      s"routeScopeStart(${funnel.getName})".source
    }
  }

  class RouteVisitControlCleanup(phase: ActionPhase)
    extends ControlTestTemporary(s"route_${funnel.getName}_control_processed")
  {
    var controlTestTemporary: ControlTestTemporary = _
    override lazy val tempVar: Var = Var(name, DeclarationScope.Visit, DataType.BOOLEAN)

    override def phase(): ActionPhase = phase

    override def generateSource()(implicit context: GlobalContext): CodeBlock =  CodeBlock { implicit cb =>
      s"if ((!${this.name}) && ${controlTestTemporary.name}) {".source
      CodeBlock { implicit cb =>
        s"routeScopeAbort(${funnel.getName})".source
      }.indent.source()
      s"} else {".source
      CodeBlock { implicit cb =>
        s"routeScopeCommit(${funnel.getName})".source
      }.indent.source()
      s"}".source()
    }
  }

  class RouteTriggerStep(plannedStep: TriggerStep, transitionKeys: List[StepTag], routeControlVarName: String)
    extends TriggerStep(plannedStep) {
    override def generateSource()(implicit context: GlobalContext): CodeBlock =  CodeBlock { implicit cb =>
      val timingExpression = plannedStep.treeStep.getTimingExpression.generateSource().head
      if (transitionKeys.nonEmpty) {
        for (k <- transitionKeys) {
          s"if (!$routeControlVarName) {".source
          s"$routeControlVarName=routeFsmStepAssert(${plannedStep.funnelName}, $k, ${plannedStep.id}, $timingExpression)".indentSource
          s"}".source
        }
      }
    }
  }

  def calculateNDFAGraph(definition:  FunnelMatchDefinition): NDFAGraph = {
    definition match {
      case list: FunnelMatchDefinitionList =>
        val followStates = list.getSteps.asScala.map{s => calculateNDFAGraph(s)}.toList
        list.getOp match {
          case FunnelMatchDefinitionList.Op.AND =>
            new SequentialNDFAGraph(followStates:_*)
          case FunnelMatchDefinitionList.Op.OR =>
            new ParallelNDFAGraph(followStates:_*)
        }
      case repeat: FunnelMatchDefinitionRepeat =>
        val followState = calculateNDFAGraph(repeat.getStep)
        new RepeatingNDFAGraph(followState, repeat.getMin, repeat.getMax)
      case bracket: FunnelMatchDefinitionStepList =>
        // if it's negating then invert the step list
        val steps = if (bracket.isNegating) {
          val notInVocabulary = bracket.getSteps.asScala.map(_.getStepId).toSet
          funnel.getSteps.filter(s => !notInVocabulary.contains(s.id)).map{s =>
            val invertStep = new FunnelMatchDefinitionStepIdContext(null, null, s.id)
            if (!bracket.isCapture)
              invertStep.setNonCapture()
            invertStep
          }
        } else {
          bracket.getSteps.asScala.toList
        }
        val followStates = steps.map{s => calculateNDFAGraph(s)}.toList
        new ParallelNDFAGraph(followStates:_*)
      case id: FunnelMatchDefinitionStepId =>
        new NodeNDFAGraph(id.getStepId, id.isCapture)
      case t =>
        throw new IllegalStateException(s"unexpected match definition type ${t.getClass}")
    }
  }

  override def selectHydraVisitLaneType(path: VisitPath)(implicit schema: MotifSchema): VisitType = {
    assert(path.isInstanceOf[FunnelVisitPath])
    val pathComponents = path.getPathAsString.split('.')

    // check the head syntax is correct
    assert(pathComponents.length > 1 && pathComponents.length < 5)
    assert(!(pathComponents.length < 2 || !(pathComponents(1) == "paths")))
    assert(!(pathComponents.length > 3 && !(pathComponents(2) == "steps")))
    if (pathComponents.length == 2) { // paths collection reference
      PHASED
    } else if (pathComponents.length == 3 && pathComponents(2) == "steps") { // steps collection reference
      SITU
    } else if (pathComponents.length == 3) { // paths field reference
      PHASED
    } else { // steps field reference
      SITU
    }
  }

}
