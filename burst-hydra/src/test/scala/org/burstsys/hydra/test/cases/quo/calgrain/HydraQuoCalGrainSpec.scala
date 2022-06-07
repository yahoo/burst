/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.calgrain

import org.burstsys.hydra.test.cases.support.HydraQuoUseCaseSpec

class HydraQuoCalGrainSpec extends HydraQuoUseCaseSpec(
  HydraQuoCalDayGrain,
  HydraQuoCalDayGrainFunc,

  HydraQuoCalHalfGrain,
  HydraQuoCalHalfGrainFunc,

  HydraQuoCalHourGrain,
  HydraQuoCalHourGrainFunc,

  HydraQuoCalMonthGrain,
  HydraQuoCalMonthGrainFunc,

  HydraQuoCalQuarterGrain,
  HydraQuoCalQuarterGrainFunc,

  HydraQuoCalWeekGrain,
  HydraQuoCalWeekGrainFunc,

  HydraQuoCalYearGrain,
  HydraQuoCalYearGrainFunc,
)
