/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.visits

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.generators.{ActionPhase, HydraLaneSourceActions, LaneSourceGenerator}

import scala.language.postfixOps

/**
 * phased visits have a block for each phase of a visit
 */

trait PhasedLaneSourceGenerator extends LaneSourceGenerator {
  self: HydraLaneSourceActions =>

  final override def generateSource(actionPhase: ActionPhase)(implicit context: GlobalContext): CodeBlock = actionPhase match {
    case ActionPhase.Before =>
      generateBeforeSource()
    case ActionPhase.Pre =>
      generatePreSource()
    case ActionPhase.Post =>
      generatePostSource()
    case ActionPhase.After =>
      generateAfterSource()
    case _ =>
      CodeBlock.Empty
  }

  final protected def generateBeforeSource()(implicit context: GlobalContext): CodeBlock = {
    CodeBlock { implicit cb =>
      // before phase code
      val controlledBeforeCode = CodeBlock { implicit cb =>
        phasedControlledActions(ActionPhase.Before).foreach(a => a.generateSource().source())
      }

        controlledBeforeCode.source()

      if (cb.nonEmpty)
        cb.prepend(s"// ${this.name}")
    }
  }

  protected def generatePreSource()(implicit context: GlobalContext): CodeBlock = {
    CodeBlock { implicit cb =>
      val earlyControlTests =
        (control.tests(ActionPhase.Pre).map(_.name).toArray[String] ++
          control.controls(ActionPhase.Pre).flatMap(c => c.generateSource().map(b => s"($b)")).toArray[String]
          ).mkString(" && ")

      val preBlockTest = if (visitControlTest.isDefined) {
        if (earlyControlTests.nonEmpty) {
          s"${visitControlTest.get.name} = $earlyControlTests".source()
          s"${visitControlTest.get.name}"
        } else
          ""
      } else {
        if (earlyControlTests.nonEmpty) {
          assert(earlyControlTests.isEmpty)
          ""
        } else
          ""
      }

      control.tests(ActionPhase.Post).foreach(c => s"${c.summaryVar.name}=false".source())

      val controlledPreCode = blockSourceGenerator.generateSource(ActionPhase.Pre, phasedControlledActions(ActionPhase.Pre))

      // protect the actions in tests to early predicates from this visit and our parent
      if (preBlockTest.nonEmpty && (control.isAbortable || controlledPreCode.nonEmpty)) {
        s"if ($preBlockTest) {".source()
        if (controlledPreCode.nonEmpty) {
          controlledPreCode.indent.source()
        }
        if (control.isAbortable) {
          s"} else {".source()
          s"abortRelation($path)".indentSource()
        }
        s"}".source()
      } else {
        controlledPreCode.source()
      }

      if (cb.nonEmpty)
        cb.prepend(s"// ${this.name}")
    }
  }

  protected def generatePostSource()(implicit context: GlobalContext): CodeBlock = {

    CodeBlock { implicit cb =>

      val postBlockTest = if (visitControlTest.nonEmpty || control.tests(ActionPhase.Post).nonEmpty || control.controls(ActionPhase.Post).nonEmpty) {
        val hasEarlyControlTests = control.tests(ActionPhase.Pre).nonEmpty || control.controls(ActionPhase.Pre).nonEmpty
        val postTests = control.controls(ActionPhase.Post).flatMap(c => c.generateSource().map(b => s"($b)")).mkString(" && ")
        val postControl = if (hasEarlyControlTests || postTests.nonEmpty) {
          if (postTests.nonEmpty)
            s"${visitControlTest.get.name} = $postTests".source()
          Array(visitControlTest.get.name)
        } else
          Array.empty[String]

        val t = s"${(control.tests(ActionPhase.Post).map(_.summaryVar.name).toArray[String] ++ postControl).mkString(" && ")}"
        t
      } else {
        assert(control.tests(ActionPhase.Post).isEmpty)
        ""
      }

      // post phase actions code
      val controlledPostCode = CodeBlock { implicit cb =>
        if (postBlockTest.nonEmpty && visitControlTest.get.needsSummary) {
          s"${visitControlTest.get.summaryVar.name} = true".source()
        }
        blockSourceGenerator.generateSource(ActionPhase.Post, phasedControlledActions(ActionPhase.Post)).source()
      }

      if (postBlockTest.nonEmpty && (control.isAbortable || controlledPostCode.nonEmpty)) {
        s"if ($postBlockTest) {".source()
        if (controlledPostCode.nonEmpty) {
          controlledPostCode.indent.source()
        }
        if (control.isAbortable) {
          s"} else {".source()
          s"abortRelation($path)".indentSource()
        }
        s"}".source()
      } else
        controlledPostCode.source()

      if (cb.nonEmpty)
        cb.prepend(s"// ${this.name}")
    }
  }

  final protected def generateAfterSource()(implicit context: GlobalContext): CodeBlock = {

    CodeBlock { implicit cb =>
      val afterBlockTest = if (visitControlTest.isDefined && visitControlTest.get.needsSummary) {
        visitControlTest.get.summaryVar.name
      } else
        ""

      // after phase code
      val controlledAfterCode = CodeBlock { implicit cb =>
        phasedControlledActions(ActionPhase.After).foreach(a => a.generateSource().source())
      }

      if (afterBlockTest.nonEmpty && controlledAfterCode.nonEmpty) {
        s"if ($afterBlockTest) {".source()
        controlledAfterCode.indent.source()
        s"}".source()
      } else
        controlledAfterCode.source()

      if (cb.nonEmpty)
        cb.prepend(s"// ${this.name}")
    }
  }
}
