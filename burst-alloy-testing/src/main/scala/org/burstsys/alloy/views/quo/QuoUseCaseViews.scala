/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views.quo

import org.burstsys.brio.flurry.provider.quo._
import org.burstsys.alloy.store.mini.MiniView
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.brio.model.schema.BrioSchema

object QuoUseCaseViews {

  val schema: BrioSchema = BrioSchema("quo")

  lazy val views: Array[MiniView] = Array(
    // default dataset - one of everything
    UnitMiniView(schema, 2, 2,
      Array(
        QuoMockUser()
      )
    ),


    // user only
    UnitMiniView(schema, 3, 3,
      Array(
        QuoMockUser(sessions = Array.empty, segments = Array.empty, channels = Array.empty, personas = Array.empty, parameters = Map.empty)
      )
    ),

    // parameters
    UnitMiniView(schema, 10, 10,
      Array(
        QuoMockUser(sessions = Array(
          QuoMockSession(
            appVersionId = 545454,
            originSourceType = 44444444,
            osVersion = 54545445454L,
            providedOrigin = "ThisIsAnOrigin",
            events = Array(
              QuoMockEvent(
                eventId = 123456,
                parameters = Map("k1" -> "v1", "k2" -> "v2")
              ),
              QuoMockEvent(
                eventId = 123457,
                parameters = Map("k3" -> "v3", "k4" -> "v4")
              ),
              QuoMockEvent(
                eventId = 123457,
                parameters = Map("k5" -> "v5", "k6" -> "v6")
              )
            )
          ),
          QuoMockSession(
            appVersionId = 323232,
            originSourceType = 666666666,
            osVersion = 4505050505L,
            providedOrigin = "ThisIsAnOriginToo",
            events = Array(
              QuoMockEvent(
                eventId = 123457,
                parameters = Map("k7" -> "v7", "k8" -> "v8")
              ),
              QuoMockEvent(
                eventId = 123456,
                parameters = Map("k9" -> "v9", "k10" -> "v10")
              ),
              QuoMockEvent(
                eventId = 123457,
                parameters = Map("k11" -> "v11", "k12" -> "v12")
              )
            ))
        ), segments = Array.empty, channels = Array.empty, personas = Array.empty, parameters = Map.empty)
      )
    ),


    UnitMiniView(schema, 11, 11,
      Array(
        QuoMockUser(deviceModelId = 111111L),
        QuoMockUser(deviceModelId = 22222222L),
        QuoMockUser(deviceModelId = 33333333L),
        QuoMockUser(deviceModelId = 44444444L)
      )
    ),

    // empty
    UnitMiniView(schema, -1, -1, Array.empty)
  )
}
