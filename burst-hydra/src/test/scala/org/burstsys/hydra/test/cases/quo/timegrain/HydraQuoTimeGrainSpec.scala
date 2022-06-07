/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.timegrain


import org.burstsys.hydra.test.cases.support.HydraQuoUseCaseSpec

class HydraQuoTimeGrainSpec extends HydraQuoUseCaseSpec(
  HydraQuoDayGrain,
  HydraQuoDayGrainFunc,
  HydraQuoHourGrain,
  HydraQuoHourGrainFunc,
  // TAKE TOO LONG
  // HydraQuoMinuteGrain,
  // HydraQuoMinuteGrainFunc,
  // HydraQuoSecondGrain,
  // HydraQuoSecondGrainFunc,
)
