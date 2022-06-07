/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views

import org.burstsys.alloy.AlloyDatasetSpec
import org.burstsys.brio.flurry.provider.unity._
import org.burstsys.brio.model.schema.BrioSchema

object AlloySmallDatasets {

  private val _unitySchema: BrioSchema = BrioSchema("unity")

  private val _quoSchema: BrioSchema = BrioSchema("quo")

  final val smallDataset_two_user_three_session = AlloyDatasetSpec(_unitySchema, 99L)

  final val smallDataset_one_user_two_sessions = AlloyDatasetSpec(_unitySchema, 27L)
  final val smallDataset_one_user_one_session = AlloyDatasetSpec(_unitySchema, 19L)
  final val smallDataset__brian_funnel = AlloyDatasetSpec(_unitySchema, 231L)

  /**
   * 2 users each with 5 sessions, each with three events
   * all objects have unique 'ids'
   */
  final val smallDataset_2_users_5_sessions = AlloyDatasetSpec(_unitySchema, 22L)

  final val smallViews: Array[UnitMiniView] = Array(
    UnitMiniView(smallDataset_two_user_three_session,
      Array(
        UnityMockUser(id = s"User1", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 3, eventType = 1, startTime = 21),
            UnityMockEvent(id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        )),
        UnityMockUser(id = s"User2", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 3, eventType = 1, startTime = 21),
            UnityMockEvent(id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        ))
      )
    ),
    UnitMiniView(smallDataset_one_user_one_session,
      Array(
        UnityMockUser(id = s"User0", sessions = Array(
          UnityMockSession(id = 1, events = Array(
            UnityMockEvent(id = 10)
          ))
        ))
      )
    ),
    UnitMiniView(smallDataset_one_user_two_sessions,
      Array(
        UnityMockUser(id = s"User0",
          sessions = Array(
            UnityMockSession(id = 1, events = Array(
              UnityMockEvent(id = 10)
            )),
            UnityMockSession(id = 2, events = Array(
              UnityMockEvent(id = 20)
            ))
          )
        )
      )
    ),
    UnitMiniView(smallDataset_2_users_5_sessions,
      Array(
        UnityMockUser(id = s"User0",
          sessions = Array(
            UnityMockSession(id = 0, startTime = 10, events = Array(
              UnityMockEvent(id = 100),
              UnityMockEvent(id = 1000),
              UnityMockEvent(id = 10000)
            )),
            UnityMockSession(id = 1, startTime = 11, events = Array(
              UnityMockEvent(id = 111),
              UnityMockEvent(id = 1111),
              UnityMockEvent(id = 11111)
            )),
            UnityMockSession(id = 2, startTime = 22, events = Array(
              UnityMockEvent(id = 222),
              UnityMockEvent(id = 2222),
              UnityMockEvent(id = 22222)
            )),
            UnityMockSession(id = 3, startTime = 33, events = Array(
              UnityMockEvent(id = 333),
              UnityMockEvent(id = 3333),
              UnityMockEvent(id = 33333)
            )),
            UnityMockSession(id = 4, startTime = 44, events = Array(
              UnityMockEvent(id = 444),
              UnityMockEvent(id = 4444),
              UnityMockEvent(id = 44444)
            ))
          )
        ),
        UnityMockUser(id = s"User1",
          sessions = Array(
            UnityMockSession(id = 5, startTime = 55, events = Array(
              UnityMockEvent(id = 555),
              UnityMockEvent(id = 5555),
              UnityMockEvent(id = 55555)
            )),
            UnityMockSession(id = 6, startTime = 66, events = Array(
              UnityMockEvent(id = 666),
              UnityMockEvent(id = 6666),
              UnityMockEvent(id = 66666)
            )
            ),
            UnityMockSession(id = 7, startTime = 77, events = Array(
              UnityMockEvent(id = 777),
              UnityMockEvent(id = 7777),
              UnityMockEvent(id = 77777)
            )),
            UnityMockSession(id = 8, startTime = 88, events = Array(
              UnityMockEvent(id = 888),
              UnityMockEvent(id = 8888),
              UnityMockEvent(id = 88888)
            )),
            UnityMockSession(id = 9, startTime = 99, events = Array(
              UnityMockEvent(id = 999),
              UnityMockEvent(id = 9999),
              UnityMockEvent(id = 99999)
            ))
          )
        )
      )
    ),
    UnitMiniView(smallDataset__brian_funnel,
      Array(
        UnityMockUser(id = s"User1", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 3, eventType = 1, startTime = 21),
            UnityMockEvent(id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        )),
        UnityMockUser(id = s"User2", sessions = Array(
          UnityMockSession(id = 1, startTime = 9, events = Array(
            UnityMockEvent(id = 2, eventType = 1, startTime = 10),
            UnityMockEvent(id = 4, eventType = 1, startTime = 11),
            UnityMockEvent(id = 6, eventType = 1, startTime = 12),
            UnityMockEvent(id = 8, eventType = 1, startTime = 13)
          )),
          UnityMockSession(id = 2, startTime = 19, events = Array(
            UnityMockEvent(id = 1, eventType = 1, startTime = 20),
            UnityMockEvent(id = 3, eventType = 1, startTime = 21),
            UnityMockEvent(id = 5, eventType = 1, startTime = 22)
          )),
          UnityMockSession(id = 3, startTime = 29, events = Array(
            UnityMockEvent(id = 11, eventType = 1, startTime = 30),
            UnityMockEvent(id = 13, eventType = 1, startTime = 31),
            UnityMockEvent(id = 15, eventType = 1, startTime = 32)
          ))
        ))
      )
    )
  )
}
