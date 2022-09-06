/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.analysis

import org.burstsys.eql._
import org.burstsys.eql.actions.Temporary
import org.burstsys.eql.generators.hydra.cubes.Cube
import org.burstsys.eql.generators.hydra.frames.PlacedControl
import org.burstsys.eql.generators.hydra.routes.Route
import org.burstsys.eql.generators.hydra.tablets.Tablet
import org.burstsys.eql.generators.hydra.utils.CodeBlock
import org.burstsys.eql.generators.hydra.utils.CodeBlock.stringToCodeBlock
import org.burstsys.eql.generators.{BlockGenerator, DeclarationScope, Var}
import org.burstsys.eql.paths.DynamicVisitPath
import org.burstsys.eql.planning.escapeIdentifierName
import org.burstsys.eql.planning.lanes.LaneControl.extractAndControls
import org.burstsys.eql.planning.queries._
import org.burstsys.motif.common.DataType
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.strings.VitalsString
class AnalysisGeneratorImpl(analysis: Query) extends BlockGenerator {

  override def generateSource()(implicit context: GlobalContext): String = {
    val analysisName = "eqlGenerated"
    context.addProperty("globalLimit", analysis.getGlobalLimit.toString)
    context.addProperty(AnalysisPropertyName, analysisName)

    /* extract the global where */
    val globalWhere = analysis.getGlobalWhere
    val globalPlacedControls = extractAndControls(globalWhere).map(gc => PlacedControl(gc, Temporary placeTemporaries gc.expression))

    // make frame sources for every select
    val cubeFrames = analysis.map{s =>
      //RelationAbort(s.visits.getSchemaRoot).placeInVisit(RESULT)(s.visits)

      new Cube(f"${s.select.name}%s", s).
        addPlacedControl(globalPlacedControls)
    }

    // make a frame source for any additional structures
    val additionalFrames = analysis.getSources.flatMap { s =>
      context.getDeclaration(s.declaredName.toLowerCase) match {
        case r: Route =>
          r.applyParameters(s.parameters)
          Some(r.addPlacedControl(globalPlacedControls))
        case t: Tablet =>
          t.applyParameters(s.parameters)
          Some(t.addPlacedControl(globalPlacedControls))
        case _ => None
      }
    }

    // put dynamic attachment points into frames so actions can be placed there
    (cubeFrames ++ additionalFrames).foreach { f =>
      f.visits.visitMap.keys.foreach {
        case dvp: DynamicVisitPath =>
          f.addAttachment(dvp)
        case _ =>
      }
    }

    val parametersList = analysis.getParameters.map{v =>
      val eName = escapeIdentifierName(v.getName)
      v.getDtype(analysis) match {
        case DataType.STRING =>
          s"$eName:${v.getDtype(analysis).toString.toLowerCase()}=null"
        case DataType.BOOLEAN =>
          s"$eName:${v.getDtype(analysis).toString.toLowerCase()}=null"
        case DataType.DOUBLE =>
          s"$eName:${v.getDtype(analysis).toString.toLowerCase()}=0.0"
        case DataType.INTEGER | DataType.LONG | DataType.SHORT =>
          s"$eName:${v.getDtype(analysis).toString.toLowerCase()}=0"
        case x: DataType =>
          throw VitalsException(s"unexpected datatype '$x'")
      }
    }

    // now generate the hydra source text
    val frameSource = CodeBlock { implicit cb =>
      cubeFrames.foreach(_.generateSource().indent.source())
      additionalFrames.foreach(_.generateSource().indent.source())
    }

    // get any declarations at the analysis level
    val variableDeclarations = (cubeFrames ++ additionalFrames).flatMap(_.getDeclarations(DeclarationScope.Analysis))
      .groupBy(_.name).values.map(_.head).filter(d => d.isInstanceOf[Var])

    val hydraSource = CodeBlock { implicit cb =>
      s"hydra ${context(AnalysisPropertyName)}(${parametersList.mkString(",")}) {".source()
      CodeBlock { implicit cb =>
        s"schema '${analysis.getSchemaName.toLowerCase()}'".source()
        if (context.getAttachments.nonEmpty) {
          s"{".source()
          CodeBlock { implicit cb =>
            context.getAttachments.foreach(e => s"${e._1.getAttachmentPath} <- ${e._2}".source())
          }.indent.source()
          s"}".source()
        }
      }.indent.source()
      variableDeclarations.foreach(_.generateDeclarationSource().indent.source())
      frameSource.source()
      s"}".source()
    }


    context.removeProperty(AnalysisPropertyName)
    hydraSource.mkString("\n").noMultipleLineEndings.trim
  }
}
