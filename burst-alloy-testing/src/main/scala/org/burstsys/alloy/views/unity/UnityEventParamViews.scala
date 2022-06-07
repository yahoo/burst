/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views.unity

import org.burstsys.alloy.store.mini.MiniView
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.alloy.views.unity.UnityGenerator.GeneratorControls
import org.burstsys.alloy.views.unity.UnityGenerator.generated
import org.burstsys.alloy.views.unity.UnityUseCaseViews.unitySchema
import org.burstsys.alloy.AlloyDatasetSpec
import org.burstsys.alloy.BurstUnitRepeatingValue

object UnityEventParamViews {

  lazy val fixed_event_parameters: AlloyDatasetSpec = AlloyDatasetSpec(unitySchema, 787L)
  lazy val float_event_parameters: AlloyDatasetSpec = AlloyDatasetSpec(unitySchema, 788L)

  lazy val unityParamViews: Array[MiniView] = Array(
    UnitMiniView(fixed_event_parameters,
      generated(userCount = 2, sessionCount = 2, eventCount = 2, parameterCount = 2,
        controls = GeneratorControls(
          eventParameters = BurstUnitRepeatingValue(
            Map("K1" -> "1"), Map("K2" -> "2"), Map("K3" -> "3"), Map("K4" -> "4"), Map("K5" -> "5"), Map("K6" -> "6"), Map("K7" -> "7")
          )
        )
      )
    ),
    UnitMiniView(float_event_parameters,
      generated(userCount = 2, sessionCount = 2, eventCount = 2, parameterCount = 2,
        controls = GeneratorControls(
          eventParameters = BurstUnitRepeatingValue(
            Map("K1" -> "1.1"), Map("K2" -> "2.2"), Map("K3" -> "3.3"), Map("K4" -> "4.4"), Map("K5" -> "5.5"), Map("K6" -> "6.6"), Map("K7" -> "7.7")
          )
        )
      )
    )

  )
}
