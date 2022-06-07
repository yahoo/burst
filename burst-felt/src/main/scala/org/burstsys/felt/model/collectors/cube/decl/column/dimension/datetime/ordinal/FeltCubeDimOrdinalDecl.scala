/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.ordinal

import org.burstsys.brio.types.BrioTypes.{BrioLongKey, BrioRelationName, BrioTypeKey, brioDataTypeNameFromKey}
import org.burstsys.felt.model.FeltException
import org.burstsys.felt.model.collectors.cube.decl.column.dimension._
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.tree.FeltLocation
import org.burstsys.felt.model.tree.source.S

trait FeltCubeDimOrdinalDecl extends AnyRef with FeltCubeDimDecl {

  final override val nodeName = "felt-cube-ordinal-dim"

  val valueType: BrioTypeKey = BrioLongKey

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // artifact planning
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override lazy
  val semantic: FeltCubeDimTimeOrdinalSem = semanticType match {
    case SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimSecOfMinSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimMinOfHrSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimHrOfDaySem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimDayOfWkSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimDayOfMonthSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimMonthOfYrSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimDayOfYrSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimWkOfYrSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC => new FeltCubeDimYrOfEraSem {
      final override val columnName: BrioRelationName = FeltCubeDimOrdinalDecl.this.columnName
    }
    case _ => throw FeltException(location, s" unknown calendar ordinal semantic=$semanticType")
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  override def reduceStatics: FeltCubeDimOrdinalDecl = new FeltCubeDimOrdinalDecl {
    sync(FeltCubeDimOrdinalDecl.this)
    final override val refName: FeltPathExpr = FeltCubeDimOrdinalDecl.this.refName
    final override val valueType: BrioTypeKey = FeltCubeDimOrdinalDecl.this.valueType
    final override val location: FeltLocation = FeltCubeDimOrdinalDecl.this.location
    final override val semanticType: FeltDimSemType = FeltCubeDimOrdinalDecl.this.semanticType
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String =
    s"$S'$columnName':${semanticType.name}[${brioDataTypeNameFromKey(valueType).toLowerCase}]"

}
