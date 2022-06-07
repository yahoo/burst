/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.grain

import org.burstsys.brio.types.BrioTypes.{BrioLongKey, BrioRelationName, BrioTypeKey, brioDataTypeNameFromKey}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.column.dimension._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.source.S

trait FeltCubeDimGrainDecl extends FeltCubeDimDecl {

  final override val nodeName = "felt-cube-grain-dim"

  val valueType: BrioTypeKey = BrioLongKey

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // artifact planning
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override lazy val semantic: FeltCubeDimColSem = {
    val sem = semanticType match {
      case SECOND_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimSecondGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case HOUR_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimHourGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case MINUTE_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimMinuteGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case DAY_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimDayGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case WEEK_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimWeekGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case MONTH_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimMonthGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case QUARTER_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimQuarterGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case HALF_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimHalfGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case YEAR_GRAIN_DIMENSION_SEMANTIC => new FeltCubeDimYearGrainSem {
        final override val columnName: BrioRelationName = FeltCubeDimGrainDecl.this.columnName
      }
      case _ => throw FeltException(location, s" unknown calendar grain semantic=$semanticType")
    }
    assert(semanticType == sem.semanticRt.semanticType)
    sem
  }
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeDimGrainDecl = new FeltCubeDimGrainDecl {
    sync(FeltCubeDimGrainDecl.this)
    final override val refName: FeltPathExpr = FeltCubeDimGrainDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeDimGrainDecl.this.valueType
    final override val location: FeltLocation = FeltCubeDimGrainDecl.this.location
    final override val semanticType: FeltDimSemType = FeltCubeDimGrainDecl.this.semanticType
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}]"

}
