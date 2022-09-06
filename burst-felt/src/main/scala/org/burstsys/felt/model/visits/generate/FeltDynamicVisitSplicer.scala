/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.visits.generate

import org.burstsys.brio.types.BrioPath.{BrioPathKey, BrioPathName}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.brio.reference.FeltBrioRef
import org.burstsys.felt.model.collectors.cube.decl.FeltCubeDecl
import org.burstsys.felt.model.collectors.route.decl.FeltRouteDecl
import org.burstsys.felt.model.collectors.runtime.FeltCollector
import org.burstsys.felt.model.frame.FeltFrameDecl
import org.burstsys.felt.model.schema.traveler.FeltTraveler
import org.burstsys.felt.model.sweep.splice.{FeltSpliceGenerator, _}
import org.burstsys.felt.model.sweep.symbols.{feltRuntimeClass, schemaRuntimeSym, sweepRuntimeClassVal}
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.visits.decl._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
 * Dynamic visits are visits (iterations over) [[FeltCollector]] based relations
 * as opposed to static schema relation visits (iterations over).  They are
 * designed and implemented by code generated into the [[FeltAnalysisDecl]] as opposed
 * to managed by shared (generated once per BrioSchema)
 * [[FeltTraveler]]. This makes them less efficient
 * than static relations both at runtime and at code generation/compile time.
 * However they can be highly optimized for a very specific analysis tree.
 * Dynamic visits represent an interframe [[FeltFrameDecl]] and inter collector
 * analysis semantic i.e. the contents of a [[FeltRouteDecl]] can be visited in
 * a [[FeltCubeDecl]] frame. The collector/frame doing the visiting is called
 * a [[FeltVisitorRef]] and the collector/frame being visited is called the
 * [[FeltVisitableRef]]
 *
 * @param analysis
 */
final case
class FeltDynamicVisitSplicer(analysis: FeltAnalysisDecl) extends FeltSpliceStore {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  val global: FeltGlobal = analysis.global

  private
  type SpliceOrdinal = Int

  // top level visit capture
  private
  val _visitCaptureMap = new mutable.HashMap[FeltVisitKey, FeltVisitCapture]

  private case
  class SpliceCaptureKey(pathKey: BrioPathKey, pathName: BrioPathName, placement: FeltPlacement, ordinal: SpliceOrdinal)

  private
  val _spliceCaptureMap = new mutable.HashMap[SpliceCaptureKey, FeltSpliceBuffer]

  private
  val _actionSpliceList = new ArrayBuffer[FeltSplice]

  private
  val _visitDeclsGenList = new ArrayBuffer[FeltSpliceGenerator]

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param cursor
   * @return
   */
  def generateRtDynamicVisitDecls(implicit cursor: FeltCodeCursor): FeltCode = {
    if (_visitDeclsGenList.isEmpty) {
      s"""|
          |${C(s"no dynamic relation visit variables")}""".stripMargin
    } else {
      val code = _visitDeclsGenList.map {
        declGen => if (declGen == FeltEmptySpliceGenerator) null else declGen(cursor)
      }.filter(_ != null).mkString
      s"""|
          |${C(s"dynamic relation visit variables")}$code
          |""".stripMargin
    }
  }

  /**
   *
   * @param cursor
   * @return
   */
  def genSwpDynamicRelations(implicit cursor: FeltCodeCursor): FeltCode = {

    def actionBodies: String = _actionSpliceList.map {
      s => s"${s.generateSpliceMethodBody}"
    }.mkString

    s"""|
        |$genRelationVisitsAndJoins$actionBodies """.stripMargin
  }

  /**
   *
   * @return
   */
  def collect: this.type = {
    // capture all visit splices
    analysis.frames foreach {
      frame =>
        frame.visits foreach {
          case visit: FeltDynamicVisitDecl =>
            visit.visitedCollector.referenceType[FeltVisitableRef] match {
              case None =>
              case Some(visitableRef) =>
                val path = visit.traverseTarget.fullPath
                val collector = frame.collectorDecl
                // capture all the info needed to splice in a dynamic visit and store in map
                val visitorRef: FeltVisitorRef = collector.refName.referenceType[FeltVisitorRef].getOrElse {
                  val frameToBeVisited = visit.visitedCollector.reference.get.frame.frameName
                  val frameDoingVisit = collector.frame.frameName
                  throw FeltException(collector,
                    s"frame '$frameDoingVisit' does not know how to visit frame '$frameToBeVisited'")
                }
                val visitKey = FeltVisitKey(visitableRef.visitableTag, visitorRef.visitorTag, path)
                _visitCaptureMap.getOrElseUpdate(
                  visitKey, FeltVisitCapture(key = visitKey, visitor = visitorRef, visitable = visitableRef)
                ).visits += visit
            }
          case _ =>
        }
    }
    genVisitSplicesFromCapture()
    this
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // IMPLEMENTATION
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private
  def genRelationVisitsAndJoins(implicit cursor: FeltCodeCursor): FeltCode = {

    def pathKeyAndOrdinalSplices(placement: FeltPlacement, pathKey: BrioPathKey, ordinal: SpliceOrdinal): Array[FeltSplice] = {
      _spliceCaptureMap.filter(
        k => k._1.pathKey == pathKey && k._1.placement == placement && k._1.ordinal == ordinal
      ).values.flatten.toArray
    }

    // splice calls at a particular path & ordinal
    def spliceCallsForOrdinal(placement: FeltPlacement, pathKey: BrioPathKey, spliceOrdinal: SpliceOrdinal)(implicit cursor: FeltCodeCursor): FeltCode = {
      val spliceCalls = pathKeyAndOrdinalSplices(placement, pathKey, spliceOrdinal).map(s => s"${s.generateSpliceMethodCall}").mkString
      s"""|$I1// ordinal=$spliceOrdinal$spliceCalls """.stripMargin
    }

    // lookup and call the right set of splice methods to call at what path in ordinal order
    val sweepRtClass = sweepRuntimeClassVal(global)(cursor indentRight 1)

    def pathCase(placement: FeltPlacement, pathKey: BrioPathKey, pathName: BrioPathName)(implicit cursor: FeltCodeCursor): FeltCode = {
      def splicesForPathKey(implicit cursor: FeltCodeCursor): FeltCode =
        pathOrdinalList.map(spliceCallsForOrdinal(placement, pathKey, _)).mkString
      s"""|
          |${I1}case $pathKey =>  // '$pathName'
          |${splicesForPathKey(cursor indentRight)}""".stripMargin
    }

    def pathForPlacementMatch(placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = {
      s"""|${I1}path match { ${pathsForPlacement(placement).map(p => pathCase(placement, p._1, p._2)(cursor indentRight)).mkString}
          |${I2}case _ =>
          |${I1}} """.stripMargin
    }

    def placementCase(placement: FeltPlacement)(implicit cursor: FeltCodeCursor): FeltCode = {
      s"""|
          |${I1}case ${placement.key} => // '${placement}'
          |${pathForPlacementMatch(placement)(cursor indentRight)} """.stripMargin
    }

    def placementMatch(implicit cursor: FeltCodeCursor): FeltCode = {
      s"""|${I1}placement match { ${placements.map(p => placementCase(p)(cursor indentRight)).mkString}
          |${I2}case _ =>
          |${I1}} """.stripMargin
    }

    s"""|
        |${C(s"dynamic relations splices")}
        |${I}@inline override
        |${I}def dynamicRelationSplices($schemaRuntimeSym: $feltRuntimeClass, path: Int, placement: Int): Unit = {$sweepRtClass;
        |${placementMatch(cursor indentRight)}
        |$I}""".stripMargin
  }

  private
  def pathOrdinalList: List[Int] = _spliceCaptureMap.toList.map(_._1.ordinal).distinct.sorted

  private
  def placements: List[FeltPlacement] = _spliceCaptureMap.toList.map(_._1.placement).distinct

  private
  def pathsForPlacement(placement: FeltPlacement): List[(BrioPathKey, BrioPathName)] =
    _spliceCaptureMap.toList.filter(_._1.placement == placement).map(t => (t._1.pathKey, t._1.pathName))

  private
  def genVisitSplicesFromCapture(): Unit = {
    // we have all the captures, create the splices
    _visitCaptureMap.values.foreach {
      visitCapture =>
        val visitor = visitCapture.visitor
        val visitable = visitCapture.visitable
        visitCapture.visits foreach {
          visit =>
            val ref = visit.traverseTarget.referenceGetOrThrow[FeltBrioRef]
            val node = ref.brioNode
            val pathName = node.pathName
            val pathKey = node.pathKey

            /**
             * a dynamic visit (a visitor frame iterating over the data in a visitable frame)
             * ''must'' be at an ''extended'' relation splice point
             */
            if (!node.relation.relationForm.isExtended)
              throw FeltException(visit, s"dynamic visit defined at a non extended target: '$pathName'")

            /**
             * because this is an extended relationship - the 'parent' must be a static schema path.
             */
            val parentNode = node.parent
            if (parentNode.relation.relationForm.isExtended)
              throw FeltException(visit, s"dynamic visit defined at a non extended target parent: '${parentNode.pathName}'")

            val parentPathKey = parentNode.pathKey
            val parentPathName = parentNode.pathName

            val visitTag = visitCapture.key.spliceTag
            val visitOrdinal = visit.ordinal

            _visitDeclsGenList += visitor.visitorSplicer.generateVisitorRtDecls(visitTag)
            _visitDeclsGenList += visitable.visitableSplicer.generateVisitableRtDecls(visitTag)

            // collect the action splices - because this is a dynamic relation, we have to generate ''bodies'' and ''calls''
            val visitActionSplices = extractActionSplices(visit, pathName, visitTag)

            // capture action splice so we can generate the bodies and the calls
            _actionSpliceList ++= visitActionSplices.map(_._2)

            // get all sub splicing for this visit
            val (visitSplices, joinSplices, cleanupSplices) = spliceVisitAlgorithm(visitTag, visitor, visitable, visitActionSplices)

            // get the top level visit splice and put in the lower level ones
            val visitSplice = FeltMultiGenSplice(
              visit.global, visit.location, visitTag, pathName = pathName, placement = FeltDynamicVisitPlace,
              generators = visitSplices, ordinal = visitOrdinal
            )

            this += visitSplice // add this splice to our store so it gets added to the sweep
            _spliceCaptureMap.getOrElseUpdate(
              SpliceCaptureKey(parentPathKey, parentPathName, FeltDynamicVisitPlace, visitSplice.ordinal),
              new FeltSpliceBuffer
            ) += visitSplice

            // add the top level join splice
            val joinSplice = FeltMultiGenSplice(
              visit.global, visit.location, visitTag, pathName = pathName, placement = FeltDynamicJoinPlace,
              generators = joinSplices, ordinal = visitOrdinal
            )

            this += joinSplice // add this splice to our store so it gets added to the sweep
            _spliceCaptureMap.getOrElseUpdate(
              SpliceCaptureKey(parentPathKey, parentPathName, FeltDynamicJoinPlace, joinSplice.ordinal),
              new FeltSpliceBuffer
            ) += joinSplice

            // add the top level join splice
            val cleanupSplice = FeltMultiGenSplice(
              visit.global, visit.location, visitTag, pathName = pathName, placement = FeltDynamicCleanupPlace,
              generators = cleanupSplices, ordinal = visitOrdinal
            )

            // add the top level cleanup splice
            this += cleanupSplice
            _spliceCaptureMap.getOrElseUpdate(
              SpliceCaptureKey(parentPathKey, parentPathName, FeltDynamicCleanupPlace, cleanupSplice.ordinal),
              new FeltSpliceBuffer
            ) += cleanupSplice
        }
    }
  }

  /**
   * splice the visit algorithm
   *
   * @param visitor
   * @param visitable
   * @param generatorList
   * @param actionSplices
   */
  private
  def spliceVisitAlgorithm(
                            visitTag: String,
                            visitor: FeltVisitorRef,
                            visitable: FeltVisitableRef,
                            actionSplices: Array[(FeltActionType, FeltSplice)]
                          ): (Array[FeltSpliceGenerator], Array[FeltSpliceGenerator], Array[FeltSpliceGenerator]) = {

    val visitGeneratorList = new ArrayBuffer[FeltSpliceGenerator]
    val joinGeneratorList = new ArrayBuffer[FeltSpliceGenerator]
    val cleanupGeneratorList = new ArrayBuffer[FeltSpliceGenerator]

    // helper to select splices for the right action type
    def actionSplicesFor(actionType: FeltActionType): Array[FeltSplice] =
      actionSplices.filter(_._1 == actionType).map(_._2)

    ////////////////////////////////////////////////////////////////////////////////
    // prepare visitor and visitable
    ////////////////////////////////////////////////////////////////////////////////

    val visitorPrepare = visitor.visitorSplicer.visitorIterationPrepare(visitTag)
    if (visitorPrepare != FeltEmptySpliceGenerator) visitGeneratorList += visitorPrepare

    val visitablePrepare = visitable.visitableSplicer.visitablePrepare(visitTag)
    if (visitablePrepare != FeltEmptySpliceGenerator) visitGeneratorList += visitablePrepare

    val visitorBeforeActions =
      visitor.visitorSplicer.visitorBeforeActions(visitTag, actionSplicesFor(FeltBeforeActionType))
    if (visitorBeforeActions != FeltEmptySpliceGenerator) visitGeneratorList += visitorBeforeActions

    ////////////////////////////////////////////////////////////////////////////////
    // LOOP BEGIN
    ////////////////////////////////////////////////////////////////////////////////

    val visitableStartLoop = visitable.visitableSplicer.visitableStartLoop(visitTag)
    if (visitableStartLoop != FeltEmptySpliceGenerator) visitGeneratorList += visitableStartLoop

    val visitableMemberPrepare = visitable.visitableSplicer.visitableMemberPrepare(visitTag)
    if (visitableMemberPrepare != FeltEmptySpliceGenerator) visitGeneratorList += visitableMemberPrepare

    val visitorMemberPrepare = visitor.visitorSplicer.visitorMemberPrepare(visitTag)
    if (visitorMemberPrepare != FeltEmptySpliceGenerator) visitGeneratorList += visitorMemberPrepare

    val visitorPreActions =
      visitor.visitorSplicer.visitorPreActions(visitTag, actionSplicesFor(FeltPreActionType))
    if (visitorPreActions != FeltEmptySpliceGenerator) visitGeneratorList += visitorPreActions

    val visitorSituActions =
      visitor.visitorSplicer.visitorSituActions(visitTag, actionSplicesFor(FeltSituActionType))
    if (visitorSituActions != FeltEmptySpliceGenerator) visitGeneratorList += visitorSituActions

    val visitableNestedIteration = visitable.visitableSplicer.visitableNestedIteration(visitTag)
    if (visitableNestedIteration != FeltEmptySpliceGenerator) visitGeneratorList += visitableNestedIteration

    val visitorPostActions =
      visitor.visitorSplicer.visitorPostActions(visitTag, actionSplicesFor(FeltPostActionType))
    if (visitorPostActions != FeltEmptySpliceGenerator) visitGeneratorList += visitorPostActions

    val visitableMemberCleanup = visitable.visitableSplicer.visitableMemberCleanup(visitTag)
    if (visitableMemberCleanup != FeltEmptySpliceGenerator) visitGeneratorList += visitableMemberCleanup

    val visitorMemberCleanup = visitor.visitorSplicer.visitorMemberCleanup(visitTag)
    if (visitorMemberCleanup != FeltEmptySpliceGenerator) visitGeneratorList += visitorMemberCleanup

    ////////////////////////////////////////////////////////////////////////////////
    // LOOP END
    ////////////////////////////////////////////////////////////////////////////////

    val visitableEndLoop = visitable.visitableSplicer.visitableEndLoop(visitTag)
    if (visitableEndLoop != FeltEmptySpliceGenerator) visitGeneratorList += visitableEndLoop

    val visitorAfterActions =
      visitor.visitorSplicer.visitorAfterActions(visitTag, actionSplicesFor(FeltAfterActionType))
    if (visitorAfterActions != FeltEmptySpliceGenerator) visitGeneratorList += visitorAfterActions

    // cleanup visitor and visitable
    val visitableCleanup = visitable.visitableSplicer.visitableCleanup(visitTag)
    if (visitableCleanup != FeltEmptySpliceGenerator) visitGeneratorList += visitableCleanup

    // after iteration and before parent post operations
    val visitorCleanup = visitor.visitorSplicer.visitorIterationCleanup(visitTag)
    if (visitorCleanup != FeltEmptySpliceGenerator) visitGeneratorList += visitorCleanup

    // after iteration and parent post operations we do all joins
    val visitorJoins = visitor.visitorSplicer.visitorJoinOperations(visitTag)
    if (visitorJoins != FeltEmptySpliceGenerator) joinGeneratorList += visitorJoins

    // after iteration and  joins just cleanup
    val visitorCleanups = visitor.visitorSplicer.visitorCleanupOperations(visitTag)
    if (visitorCleanups != FeltEmptySpliceGenerator) cleanupGeneratorList += visitorCleanups

    (visitGeneratorList.toArray, joinGeneratorList.toArray, cleanupGeneratorList.toArray)
  }

  /**
   * capture action splices within the visit
   *
   * @param visit
   * @param pathName
   * @param visitTag
   * @return
   */
  private
  def extractActionSplices(visit: FeltDynamicVisitDecl, pathName: BrioPathName, visitTag: String): Array[(FeltActionType, FeltSplice)] = {
    visit.actions.map {
      action =>
        if (action.expressionBlock.nonEmpty) {
          val actionTag = s"${visitTag}_${action.actionType}"
          (action.actionType,
            FeltExprSplice(
              visit.global, visit.location,
              spliceTag = actionTag,
              pathName = pathName,
              placement = action.actionType.placement,
              expression = action.expressionBlock,
              ordinal = visit.ordinal
            )
          )
        } else null
    }.filter(_ != null)
  }

}
