/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.visits

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.routes.Route
import org.burstsys.eql.generators.hydra.tablets.Tablet
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.generators.{ActionPhase, DeclarationScope, HydraVisitLanes, VisitSourceGenerator}
import org.burstsys.eql.paths.DynamicVisitPath

import scala.language.postfixOps

trait PhasedVisitSourceGenerator extends VisitSourceGenerator {
  self: HydraVisitLanes =>

  final override def generateSource()(implicit context: GlobalContext): CodeBlock = {
    val visitLabel = path match {
      case dvp: DynamicVisitPath =>
        context.getDeclaration(dvp.getLocalRoot.getNavigatorId) match {
          case tablet: Tablet =>
            val src = tablet.hydraVisits.geneterateRootPost()
          case route: Route =>
        }
        if (context.getAttachment(dvp) != null) {
          s"$dvp ${dvp.getAttachmentPath}.${context.getAttachment(dvp)}"
        } else {
          s"$dvp ${dvp.getAttachmentPath}"
        }
      case _ =>
        s"$path"
    }

    val work  = CodeBlock { implicit cb =>
      // hack to do cleanup of visited dynamic frames before they are iterated by another visiting frame
      val additionalWork: Iterator[CodeBlock] = {
        path match {
          case dvp: DynamicVisitPath =>
            context.getDeclaration(dvp.getLocalRoot.getNavigatorId) match {
              case tablet: Tablet =>
                tablet.hydraVisits.geneterateRootPost()
              case route: Route =>
                route.hydraVisits.geneterateRootPost()
            }
          case _ =>
            Iterator.empty
        }
      }

      generatePhase(ActionPhase.Before, additionalWork)

      generatePrePost()

      generatePhase(ActionPhase.After)
    }

    // build the entire visit
    // schema.getRootFieldName}
    if (work.nonEmpty)
      CodeBlock { implicit cb =>
        s"$visitLabel => {".source()
        generateDeclarationsSource(DeclarationScope.Visit).foreach(_.indentSource)
        work.indent.source
        s"}".source()
      }
    else
      CodeBlock.Empty
  }

  protected def generatePrePost()(implicit cb: CodeBlock, context: GlobalContext): Unit = {
    for (phase <- List(ActionPhase.Pre, ActionPhase.Post)) {
      generatePhase(phase)
    }
  }

  protected def generatePhase(phase: ActionPhase, additionalWork: Iterator[CodeBlock] = Iterator.empty)(implicit cb: CodeBlock, context: GlobalContext): Unit = {
    val work = traverseLanes.map(_ generateSource phase).filter(_.nonEmpty)
    if (work.nonEmpty || additionalWork.nonEmpty) {
      // build the phase hydra
      s"${phase.toString.toLowerCase} => {".source
      additionalWork.foreach(_.indent.source)
      work.foreach(_.indent.source)
      s"}".source
    }
  }

}
