/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views.unity

import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.alloy.AlloyDatasetSpec
import org.burstsys.alloy.store.mini.MiniView
import org.burstsys.alloy.views.AlloySmallDatasets.smallViews
import org.burstsys.alloy.views.unity.UnityEventParamViews.unityParamViews
import org.burstsys.alloy.views.unity.UnityGenerator.defaultGeneratorControls
import org.burstsys.alloy.views.unity.UnityGenerator.generated
import org.burstsys.alloy.views.AlloySmallDatasets
import org.burstsys.alloy.views.UnitMiniView
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.vitals.time.VitalsTimeZones
import org.joda.time._
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import scala.language.implicitConversions

object UnityUseCaseViews {

  lazy private val _1 = unityParamViews // force ordering
  lazy private val _2 = AlloySmallDatasets.smallViews // force ordering

  lazy val unitySchema: BrioSchema = BrioSchema("unity")

  lazy val over_200_200: AlloyDatasetSpec = AlloyDatasetSpec(unitySchema, 200L)

  lazy val over_666_666: AlloyDatasetSpec = AlloyDatasetSpec(unitySchema, 666L)
  lazy val over_9999_9999: AlloyDatasetSpec = AlloyDatasetSpec(unitySchema, 9999L)

  lazy val over_100_100: AlloyDatasetSpec = AlloyDatasetSpec(unitySchema, 100L)

  lazy val over_73_73: AlloyDatasetSpec = AlloyDatasetSpec(unitySchema, 73L)

  val f: DateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy").withZone(DateTimeZone.forID(VitalsTimeZones.VitalsDefaultTimeZoneName))
  val nowTime: DateTime = f.parseDateTime("11/02/2018")

  private implicit def dateTimeToMillis(t: DateTime): Long = t.getMillis

  lazy val views: Array[MiniView] = unityParamViews ++ smallViews ++ _localViews

  private lazy val _localViews: Array[MiniView] = Array(
    UnitMiniView(over_100_100,
      Array(
        UnityMockUser(
          id = s"User#${defaultGeneratorControls.userIdGenerator.incrementAndGet}",
          application = UnityMockApplication(id = 12345L),
          sessions = Array(
            UnityMockSession()
          )
        ),
        UnityMockUser(
          id = s"User#${defaultGeneratorControls.userIdGenerator.incrementAndGet}",
          application = UnityMockApplication(id = 234567),
          sessions = Array(
            UnityMockSession(),
            UnityMockSession()
          )
        ),
        UnityMockUser(
          id = s"User#${defaultGeneratorControls.userIdGenerator.incrementAndGet}",
          application = UnityMockApplication(id = 345678),
          sessions = Array(
            UnityMockSession(),
            UnityMockSession(),
            UnityMockSession()
          )
        ),
        UnityMockUser(
          id = s"User#${defaultGeneratorControls.userIdGenerator.incrementAndGet}",
          application = UnityMockApplication(id = 456789),
          sessions = Array(
            UnityMockSession(),
            UnityMockSession(),
            UnityMockSession(),
            UnityMockSession()
          )
        )
      )
    ),

    UnitMiniView(unitySchema, 150, 150,
      Array(
        UnityMockUser(
          id = s"User#${defaultGeneratorControls.userIdGenerator.incrementAndGet}",
          application = UnityMockApplication(
            id = 12345L,
            firstUse = UnityMockUse(appVersion = UnityMockAppVersion(id = 123)),
            lastUse = UnityMockUse(appVersion = UnityMockAppVersion(id = 456)),
            mostUse = UnityMockUse(appVersion = UnityMockAppVersion(id = 789))
          ),
          sessions = Array(
            UnityMockSession(appVersion = UnityMockAppVersion(id = 1010)),
            UnityMockSession(appVersion = UnityMockAppVersion(id = 1111))
          )
        )
      )
    ),


    UnitMiniView(over_73_73,
      Array(
        UnityMockUser(
          id = s"User#${defaultGeneratorControls.userIdGenerator.incrementAndGet}",
          sessions = Array(
            UnityMockSession(
              id = 1,
              events = Array(
                UnityMockEvent(
                  id = 1,
                  eventType = 1
                ),
                UnityMockEvent(
                  id = 2,
                  eventType = 1
                ),
                UnityMockEvent(
                  id = 3,
                  eventType = 1
                )
              )
            )
          )
        )
      )
    ),

    UnitMiniView(over_200_200,
      generated(userCount = 50, sessionCount = 25, eventCount = 10, parameterCount = 5)
    ),

    UnitMiniView(unitySchema, 210, 210,
      generated(userCount = 7, sessionCount = 11, eventCount = 17, parameterCount = 1)
    ),

    UnitMiniView(unitySchema, 666, 666,
      generated(userCount = 100, sessionCount = 25, eventCount = 100, parameterCount = 5)
    ),

    UnitMiniView(unitySchema, 707, 707,
      Array(
        UnityMockUser(id = null)
      )
    ),

    UnitMiniView(unitySchema, 777, 777,
      generated(userCount = 5000, sessionCount = 1, eventCount = 0)
    ),

    UnitMiniView(unitySchema, 9999, 9999,
      generated(userCount = 5, sessionCount = 100, eventCount = 3000)
    ),

    UnitMiniView(unitySchema, 100, 300,
      Array(
        UnityMockUser(
          id = "Id1",
          application = UnityMockApplication(id = 12345L),
          sessions = Array(
            UnityMockSession(startTime = nowTime minus Days.days(30))
          )
        ),
        UnityMockUser(
          id = "Id2",
          application = UnityMockApplication(id = 12345L),
          sessions = Array(
            UnityMockSession(startTime = nowTime minus Days.days(29)),
            UnityMockSession(startTime = nowTime minus Days.days(28))
          )
        ),
        UnityMockUser(
          id = "Id3",
          application = UnityMockApplication(id = 12345L),
          sessions = Array(
            UnityMockSession(startTime = nowTime minus Days.days(27)),
            UnityMockSession(startTime = nowTime minus Days.days(26))
          )
        ),
        UnityMockUser(
          id = "Id4",
          application = UnityMockApplication(id = 12345L),
          sessions = Array(
            UnityMockSession(startTime = nowTime minus Days.days(25),
              events = Array(
                UnityMockEvent(id = 1, startTime = nowTime minus Days.days(25) plus Seconds.seconds(1)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(25) plus Seconds.seconds(2)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(25) plus Seconds.seconds(3)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(25) plus Seconds.seconds(4))
              )
            ),
            UnityMockSession(startTime = nowTime minus Days.days(24),
              events = Array(
                UnityMockEvent(id = 1, startTime = nowTime minus Days.days(24) plus Seconds.seconds(1)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(24) plus Seconds.seconds(2)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(24) plus Seconds.seconds(3)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(24) plus Seconds.seconds(4))
              )
            ),
            UnityMockSession(startTime = nowTime minus Days.days(23),
              events = Array(
                UnityMockEvent(id = 1, startTime = nowTime minus Days.days(23) plus Seconds.seconds(1)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(23) plus Seconds.seconds(2)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(23) plus Seconds.seconds(3)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(23) plus Seconds.seconds(4))
              )
            ),
            UnityMockSession(startTime = nowTime minus Days.days(22),
              events = Array(
                UnityMockEvent(id = 1, startTime = nowTime minus Days.days(22) plus Seconds.seconds(1)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(22) plus Seconds.seconds(2)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(22) plus Seconds.seconds(3)),
                UnityMockEvent(id = 2, startTime = nowTime minus Days.days(22) plus Seconds.seconds(4),
                  parameters = Map("one" -> "1", "two" -> "2"))
              )
            )
          )
        )
      )
    )
  )
}
