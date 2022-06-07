/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.kryo

import org.burstsys.fabric.execution.model.execute.invoke.{FabricInvocationContext, FabricParameterizationContext}
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive._
import org.burstsys.felt.model.collectors.cube.decl.column.aggregation.take.{FeltCubeAggTakeSemRt, FeltCubeBottomTakeSemMode, FeltCubeTakeSemMode, FeltCubeTopTakeSemMode}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.coerce.{COERCE_DIMENSION_SEMANTIC, FeltCubeDimCoerceSemRt}
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.duration._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.grain._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.`enum`._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.split._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.ordinal._
import org.burstsys.felt.model.collectors.cube.decl.column.dimension.verbatim.{FeltCubeDimVerbatimSemRt, VERBATIM_DIMENSION_SEMANTIC}
import org.burstsys.felt.model.collectors.cube.plane.FeltCubePlaneContext
import org.burstsys.felt.model.collectors.cube.runtime.FeltCubeOrdinalMap
import org.burstsys.felt.model.collectors.route.decl.graph.{FeltRouteEdge, FeltRouteTransition}
import org.burstsys.felt.model.collectors.route.plane.FeltRoutePlaneContext
import org.burstsys.felt.model.collectors.shrub.plane.FeltShrubPlaneContext
import org.burstsys.felt.model.collectors.tablet.plane.FeltTabletPlaneContext
import org.burstsys.vitals.kryo.{VitalsKryoCatalogProvider, _}

import java.util.concurrent.atomic.AtomicInteger

/**
 * Kryo Serialized Class Register
 */
class FeltKryoCatalog extends VitalsKryoCatalogProvider {

  val key = new AtomicInteger(feltCatalogStart)
  val kryoClasses: Array[VitalsKryoClassPair] =
    key synchronized {
      Array(
        // planes
        (key.getAndIncrement, classOf[FeltCubePlaneContext]),
        (key.getAndIncrement, classOf[FeltRoutePlaneContext]),
        (key.getAndIncrement, classOf[FeltTabletPlaneContext]),
        (key.getAndIncrement, classOf[FeltShrubPlaneContext]),

        // aggregations
        (key.getAndIncrement, SUM_AGGREGATION_SEMANTIC.getClass),
        (key.getAndIncrement, MIN_AGGREGATION_SEMANTIC.getClass),
        (key.getAndIncrement, MAX_AGGREGATION_SEMANTIC.getClass),
        (key.getAndIncrement, UNIQUE_AGGREGATION_SEMANTIC.getClass),
        (key.getAndIncrement, TOP_AGGREGATION_SEMANTIC.getClass),
        (key.getAndIncrement, BOTTOM_AGGREGATION_SEMANTIC.getClass),
        (key.getAndIncrement, PROJECT_AGGREGATION_SEMANTIC.getClass),

        // grouping dimensions
        (key.getAndIncrement, COERCE_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, ENUM_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, SPLIT_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, VERBATIM_DIMENSION_SEMANTIC.getClass),

        // durations dimensions
        (key.getAndIncrement, WEEK_DURATION_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, DAY_DURATION_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, HOUR_DURATION_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, MINUTE_DURATION_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, SECOND_DURATION_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, DAY_OF_WEEK_ORDINAL_DIMENSION_SEMANTIC.getClass),

        // ordinals dimensions
        (key.getAndIncrement, DAY_OF_MONTH_ORDINAL_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, MONTH_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, DAY_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, WEEK_OF_YEAR_ORDINAL_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, HOUR_OF_DAY_ORDINAL_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, MINUTE_OF_HOUR_ORDINAL_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, SECOND_OF_MINUTE_ORDINAL_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, YEAR_OF_ERA_ORDINAL_DIMENSION_SEMANTIC.getClass),

        // grains dimensions
        (key.getAndIncrement, QUARTER_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, HALF_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, MONTH_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, DAY_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, WEEK_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, HOUR_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, MINUTE_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, SECOND_GRAIN_DIMENSION_SEMANTIC.getClass),
        (key.getAndIncrement, YEAR_GRAIN_DIMENSION_SEMANTIC.getClass),

        // route semantics
        (key.getAndIncrement, classOf[FeltRouteEdge]),
        (key.getAndIncrement, classOf[FeltRouteTransition]),

        // cube semantics
        (key.getAndIncrement, classOf[FeltCubeOrdinalMap]),

        // take semantics
        (key.getAndIncrement, classOf[FeltCubeAggTakeSemRt]),
        (key.getAndIncrement, classOf[FeltCubeTakeSemMode]),

        // aggregation semantics
        (key.getAndIncrement, classOf[Array[FeltCubeAggSemRt]]),
        (key.getAndIncrement, classOf[Array[Array[FeltCubeAggSemRt]]]),
        (key.getAndIncrement, classOf[Array[Array[FeltCubeDimSemRt]]]),

        (key.getAndIncrement, classOf[FeltCubeAggSumSemRt]),
        (key.getAndIncrement, classOf[FeltCubeAggProjectSemRt]),
        (key.getAndIncrement, classOf[FeltCubeAggUniqueSemRt]),
        (key.getAndIncrement, classOf[FeltCubeAggMaxSemRt]),
        (key.getAndIncrement, classOf[FeltCubeAggMinSemRt]),

        // dimension semantics
        (key.getAndIncrement, classOf[Array[FeltCubeDimSemRt]]),

        (key.getAndIncrement, classOf[FabricParameterizationContext]),
        (key.getAndIncrement, classOf[FabricInvocationContext]),
        // base Semantics
        (key.getAndIncrement, classOf[FeltCubeDimCoerceSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimVerbatimSemRt]),

        // enum semantics
        (key.getAndIncrement, classOf[FeltCubeDimBoolEnumSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimByteEnumSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimShortEnumSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimIntEnumSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimLongEnumSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimDoubleEnumSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimStrEnumSemRt]),

        // split semantics
        (key.getAndIncrement, classOf[FeltCubeDimBooleanSplitSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimByteSplitSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimShortSplitSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimIntSplitSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimLongSplitSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimDoubleSplitSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimStringSplitSemRt]),

        // ordinal  semantics
        (key.getAndIncrement, classOf[FeltCubeDimYearOfEraSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimHourOfDaySemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimDayOfWeekSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimWeekOfYearSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimDayOfMonthSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimMonthOfYearSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimDayOfYearSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimMinuteOfHourSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimSecondOfMinuteSemRt]),

        // grain  semantics
        (key.getAndIncrement, classOf[FeltCubeDimYearGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimQuarterGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimHalfGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimMonthGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimWeekGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimDayGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimHourGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimMinuteGrainSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimSecondGrainSemRt]),

        // duration  semantics
        (key.getAndIncrement, classOf[FeltCubeDimWeekDurSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimDayDurSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimHourDurSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimMinuteDurSemRt]),
        (key.getAndIncrement, classOf[FeltCubeDimSecondDurSemRt]),


        (key.getAndIncrement, FeltCubeTopTakeSemMode.getClass),
        (key.getAndIncrement, FeltCubeBottomTakeSemMode.getClass)
      )
    }
}
