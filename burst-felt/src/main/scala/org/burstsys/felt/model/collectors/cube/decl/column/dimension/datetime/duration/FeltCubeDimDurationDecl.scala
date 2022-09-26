/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.duration

import org.burstsys.brio.types.BrioTypes.{BrioLongKey, BrioRelationName, BrioTypeKey, brioDataTypeNameFromKey}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.column.dimension._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.source.S

trait FeltCubeDimDurationDecl extends FeltCubeDimDecl {

  final override lazy val semantic: FeltCubeDimDurationSem = semanticType match {
    case WEEK_DURATION_DIMENSION_SEMANTIC => new FeltCubeDimWeekDurSem {
      final override val columnName: BrioRelationName = FeltCubeDimDurationDecl.this.columnName
    }
    case DAY_DURATION_DIMENSION_SEMANTIC => new FeltCubeDimDayDurSem {
      final override val columnName: BrioRelationName = FeltCubeDimDurationDecl.this.columnName
    }
    case HOUR_DURATION_DIMENSION_SEMANTIC => new FeltCubeDimHourDurSem {
      final override val columnName: BrioRelationName = FeltCubeDimDurationDecl.this.columnName
    }
    case MINUTE_DURATION_DIMENSION_SEMANTIC => new FeltCubeDimMinuteDurSem {
      final override val columnName: BrioRelationName = FeltCubeDimDurationDecl.this.columnName
    }
    case SECOND_DURATION_DIMENSION_SEMANTIC => new FeltCubeDimSecondDurSem {
      final override val columnName: BrioRelationName = FeltCubeDimDurationDecl.this.columnName
    }
    case _ => throw FeltException(location, s" unknown time grain semantic=$semanticType")
  }
  final override val nodeName = "felt-cube-duration-dim"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // artifact planning
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  val valueType: BrioTypeKey = BrioLongKey

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}]"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeDimDurationDecl = new FeltCubeDimDurationDecl {
    sync(FeltCubeDimDurationDecl.this)
    final override val refName: FeltPathExpr = FeltCubeDimDurationDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeDimDurationDecl.this.valueType
    final override val location: FeltLocation = FeltCubeDimDurationDecl.this.location
    final override val semanticType: FeltDimSemType = FeltCubeDimDurationDecl.this.semanticType
  }

}
