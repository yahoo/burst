/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity


import org.burstsys.hydra.test.cases.support.HydraUnityUseCaseSpec
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase04
import org.burstsys.hydra.test.cases.unity.conditional.HydraUnityCase05
import org.burstsys.hydra.test.cases.unity.counts.HydraUnityCase01
import org.burstsys.hydra.test.cases.unity.dimensioning.HydraUnityCase02
import org.burstsys.hydra.test.cases.unity.enums.HydraUnityEnumCase00
import org.burstsys.hydra.test.cases.unity.exprblk.HydraUnityExprBlk00
import org.burstsys.hydra.test.cases.unity.exprblk.HydraUnityExprBlk01
import org.burstsys.hydra.test.cases.unity.exprblk.HydraUnityExprBlk02
import org.burstsys.hydra.test.cases.unity.exprblk.HydraUnityExprBlk03
import org.burstsys.hydra.test.cases.unity.maps.HydraUnityCase12
import org.burstsys.hydra.test.cases.unity.maps.HydraUnityCase14
import org.burstsys.hydra.test.cases.unity.maps.HydraUnityCase20
import org.burstsys.hydra.test.cases.unity.nested.HydraUnityCase03
import org.burstsys.hydra.test.cases.unity.nested.HydraUnityCase13
import org.burstsys.hydra.test.cases.unity.offaxis.HydraUnityOffAxis00
import org.burstsys.hydra.test.cases.unity.offaxis.HydraUnityOffAxis01
import org.burstsys.hydra.test.cases.unity.offaxis.HydraUnityOffAxis02
import org.burstsys.hydra.test.cases.unity.offaxis.HydraUnityOffAxis03
import org.burstsys.hydra.test.cases.unity.parallel.HydraUnityCase22
import org.burstsys.hydra.test.cases.unity.refscalar.HydraUnityCase08
import org.burstsys.hydra.test.cases.unity.refscalar.HydraUnityCase09
import org.burstsys.hydra.test.cases.unity.refscalar._

import scala.language.postfixOps

class HydraUnityUseCases extends HydraUnityUseCaseSpec(
  HydraUnityCase01,
  HydraUnityCase02,
  HydraUnityCase03,
  HydraUnityCase04,
  HydraUnityCase05,
  HydraUnityCase06,
  HydraUnityCase07,
  HydraUnityCase08,
  HydraUnityCase09,
  HydraUnityCase10,
  //  HydraUnityCase11, // broken
  HydraUnityCase12,
  HydraUnityCase13,
  HydraUnityCase14,
  HydraUnityCase16,
  // ...
  HydraUnityCase20,
  // ...
  HydraUnityCase22,
  //  HydraUnityCase24, // broken
  // ...
  HydraUnityCase26,

  //HydraUnityParaCase01
  HydraUnityExprBlk00,
  HydraUnityExprBlk01,
  HydraUnityExprBlk02,
  HydraUnityExprBlk03,

  HydraUnityOffAxis00,
  HydraUnityOffAxis01,
  HydraUnityOffAxis02,
  HydraUnityOffAxis03,

  HydraUnityEnumCase00,

  //  HydraUnityFramesCase00, // broken
  //  HydraUnityFramesCase01, // broken
)
