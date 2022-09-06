/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.visits

import org.burstsys.eql.GlobalContext
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.generators.{ActionPhase, HydraLaneSourceActions}

trait SituLaneSourceGenerator extends PhasedLaneSourceGenerator {
  self: HydraLaneSourceActions =>

  override def generatePreSource()(implicit context: GlobalContext): CodeBlock = CodeBlock.Empty

  override def generatePostSource()(implicit context: GlobalContext): CodeBlock = {

    CodeBlock { implicit cb =>
      // buiid the situ visit hydra
      val controlTests = (control.tests(ActionPhase.Pre).map(_.name) ++
        control.controls(ActionPhase.Pre).flatMap(c => c.generateSource().map(b => s"($b)")) ++
        control.controls(ActionPhase.Post).flatMap(c => c.generateSource().map(b => s"($b)"))
        ).mkString(" && ")

      val blockTest = if (visitControlTest.isDefined) {
        assert(controlTests.nonEmpty)
        s"${visitControlTest.get.name} = $controlTests".source()
        s"${visitControlTest.get.name}"
      } else {
        assert(controlTests.isEmpty)
        ""
      }

      assert(control.tests(ActionPhase.Post).isEmpty)

      // val situCode = blockSourceGenerator.generateSource((phasedControlledActions(ActionPhase.Pre) ++ phasedControlledActions(ActionPhase.Post).iterator)
      val situCode = blockSourceGenerator.generateSource(ActionPhase.Pre,
        phasedControlledActions(ActionPhase.Pre) ++ phasedControlledActions(ActionPhase.Post))

      if (blockTest.nonEmpty) {
        s"if ($blockTest) {".source()
        if (visitControlTest.get.needsSummary)
          s"${visitControlTest.get.summaryVar.name} = true".indentSource()
        situCode.indent.source()
        s"}".source()
      } else
        situCode.source()

      if (situCode.nonEmpty)
        situCode.prepend(s"// ${this.name}")
    }
  }
}
