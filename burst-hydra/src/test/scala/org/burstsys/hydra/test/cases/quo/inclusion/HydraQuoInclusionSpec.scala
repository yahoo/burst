/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.inclusion

import org.burstsys.hydra.test.cases.support.HydraQuoUseCaseSpec
/**
  *
  */
class HydraQuoInclusionSpec extends HydraQuoUseCaseSpec(
    HydraQuoRangeInclusion,
    HydraQuoInvertedRangeInclusion,
    HydraQuoInlineSetInclusion,
    HydraQuoInvertedInlineSetInclusion,
    HydraQuoInlineSetInclusionVar,
    HydraQuoInlineSetInclusionExpressionVar,
    HydraQuoRangeInclusionVar,
    HydraQuoRangeInclusionExpressionVar,
    /* TODO not working yet
        HydraQuoRefSetInclusion,
        HydraQuoInvertedRefSetInclusion,
        HydraQuoParamSetInclusion
    */
)
