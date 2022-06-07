/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.parallel

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityCase24 extends HydraUseCase(100, 100, "quo") {

  //  override val sweep: BurstHydraSweep = new B71882B91BF9C45D9B97023F85E9AA0D0

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   query I00000 {
       |      cube I00000:user {
       |         limit = 1
       |         aggregates {
       |            userCount:sum[long]
       |         }
       |      }
       |      user ⇒ {
       |
       |         post ⇒          {
       |                        I00000.userCount = 1
       |         }
       |      }
       |   }
       |   query I00001 {
       |      cube I00001:user {
       |         limit = 1
       |         aggregates {
       |            sessionCount:sum[long]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         post ⇒          {
       |                        I00001.sessionCount = 1
       |         }
       |      }
       |   }
       |   query I00002 {
       |      cube I00002:user {
       |         limit = 1
       |         aggregates {
       |            eventCount:sum[long]
       |         }
       |      }
       |      user.sessions.events ⇒ {
       |
       |         post ⇒          {
       |                        I00002.eventCount = 1
       |         }
       |      }
       |   }
       |   query I00003 {
       |      cube I00003:user {
       |         limit = 1
       |         aggregates {
       |            minSessionTime:min[long]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         post ⇒          {
       |                        I00003.minSessionTime = user.sessions.startTime
       |         }
       |      }
       |   }
       |   query I00004 {
       |      cube I00004:user {
       |         limit = 1
       |         aggregates {
       |            maxSessionTime:max[long]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         post ⇒          {
       |                        I00004.maxSessionTime = user.sessions.startTime
       |         }
       |      }
       |   }
       |   query I00005 {
       |      cube I00005:user {
       |         limit = 1000
       |         aggregates {
       |            users:sum[long]
       |         }
       |         dimensions {
       |            deviceModelIds:verbatim[long]
       |         }
       |      }
       |      user ⇒ {
       |
       |         pre ⇒          {
       |                        I00005.deviceModelIds = user.deviceModelId
       |         }
       |         post ⇒          {
       |                        I00005.users = 1
       |         }
       |      }
       |   }
       |   query I00006 {
       |      cube I00006:user {
       |        limit = 400
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            osVersionIds:verbatim[long]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         pre ⇒          {
       |                        I00006.osVersionIds = user.sessions.osVersion
       |         }
       |         post ⇒          {
       |                        I00006.sessions = 1
       |         }
       |      }
       |   }
       |   query I00007 {
       |      cube I00007:user {
       |        limit = 400
       |         aggregates {
       |            projects:sum[long]
       |         }
       |         cube user.project {
       |            dimensions {
       |               languageIds:verbatim[long]
       |            }
       |         }
       |      }
       |      user.project ⇒ {
       |
       |         pre ⇒          {
       |                        I00007.languageIds = user.project.languageId
       |         }
       |         post ⇒          {
       |                        insert(I00007)
       |         }
       |      }
       |      user ⇒ {
       |
       |         post ⇒          {
       |                        I00007.projects = 1
       |         }
       |      }
       |   }
       |   query I00008 {
       |      cube I00008:user {
       |         limit = 1000
       |         aggregates {
       |            users:sum[long]
       |         }
       |         cube user.sessions {
       |            dimensions {
       |               appVersionIds:verbatim[long]
       |            }
       |         }
       |      }
       |      user ⇒ {
       |
       |         post ⇒          {
       |                        I00008.users = 1
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         pre ⇒          {
       |                        I00008.appVersionIds = user.sessions.appVersionId
       |         }
       |         post ⇒          {
       |                        insert(I00008)
       |         }
       |      }
       |   }
       |   query I00009 {
       |      cube I00009:user {
       |         limit = 100
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            providedOrigins:verbatim[string]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         pre ⇒          {
       |                        I00009.providedOrigins = user.sessions.providedOrigin
       |         }
       |         post ⇒          {
       |                        I00009.sessions = 1
       |         }
       |      }
       |   }
       |   query I00010 {
       |      cube I00010:user {
       |         limit = 50
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            mappedOrigin:verbatim[long]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         pre ⇒          {
       |                        I00010.mappedOrigin = user.sessions.mappedOrigin
       |         }
       |         post ⇒          {
       |                        I00010.sessions = 1
       |         }
       |      }
       |   }
       |   query I00011 {
       |      cube I00011:user {
       |         limit = 10
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            originSourceType:verbatim[long]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         pre ⇒          {
       |          I00011.originSourceType = user.sessions.originSourceType
       |         }
       |         post ⇒          {
       |                        I00011.sessions = 1
       |         }
       |      }
       |   }
       |   query I00012 {
       |      cube I00012:user {
       |         limit = 10
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            originMethodType:verbatim[long]
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         pre ⇒          {
       |            I00012.originMethodType = user.sessions.originMethodType
       |         }
       |         post ⇒          {
       |            I00012.sessions = 1
       |         }
       |      }
       |   }
       |   query I00013 {
       |      cube I00013:user {
       |         limit = 1400
       |         aggregates {
       |            eventFrequency:sum[long]
       |         }
       |         dimensions {
       |            events:verbatim[long]
       |         }
       |      }
       |      user.sessions.events ⇒ {
       |
       |         pre ⇒          {
       |            I00013.events = user.sessions.events.eventId
       |         }
       |         post ⇒          {
       |            I00013.eventFrequency = 1
       |         }
       |      }
       |   }
       |   query I00014 {
       |      cube I00014:user {
       |         limit = 14000
       |         aggregates {
       |            eventParameterFrequency:sum[long]
       |         }
       |         dimensions {
       |            eventParameters:verbatim[long]
       |         }
       |         cube user.sessions.events.parameters {
       |            dimensions {
       |               eventParameterKey:verbatim[string]
       |            }
       |         }
       |      }
       |      user.sessions.events.parameters ⇒ {
       |
       |         situ ⇒          {
       |            I00014.eventParameterKey = key(user.sessions.events.parameters)
       |            insert(I00014)
       |         }
       |      }
       |      user.sessions.events ⇒ {
       |
       |         pre ⇒          {
       |            I00014.eventParameters = user.sessions.events.eventId
       |         }
       |         post ⇒          {
       |            I00014.eventParameterFrequency = 1
       |         }
       |      }
       |   }
       |   query I00015 {
       |      cube I00015:user {
       |         limit = 2000
       |         aggregates {
       |            campaignIdFrequency:sum[long]
       |         }
       |         cube user.channels {
       |            dimensions {
       |               campaignId:verbatim[long]
       |            }
       |         }
       |      }
       |      user.channels ⇒ {
       |
       |         pre ⇒          {
       |                        I00015.campaignId = user.channels.channelId
       |         }
       |         post ⇒          {
       |                        insert(I00015)
       |         }
       |      }
       |      user ⇒ {
       |
       |         post ⇒          {
       |                        I00015.campaignIdFrequency = 1
       |         }
       |      }
       |   }
       |   query I00016 {
       |      cube I00016:user {
       |         limit = 200
       |         aggregates {
       |            channelIdFrequency:sum[long]
       |         }
       |         cube user.channels {
       |            dimensions {
       |               channelId:verbatim[long]
       |            }
       |         }
       |      }
       |      user.channels ⇒ {
       |
       |         pre ⇒          {
       |                        I00016.channelId = user.channels.networkId
       |         }
       |         post ⇒          {
       |            insert(I00016)
       |         }
       |      }
       |      user ⇒ {
       |
       |         post ⇒          {
       |            I00016.channelIdFrequency = 1
       |         }
       |      }
       |   }
       |   query I00017 {
       |      cube I00017:user {
       |         limit = 200
       |         aggregates {
       |            sessionParameterFrequency:sum[long]
       |         }
       |         cube user.sessions.parameters {
       |            dimensions {
       |               sessionParameterKeys:verbatim[string]
       |            }
       |         }
       |      }
       |      user.sessions.parameters ⇒ {
       |
       |         situ ⇒          {
       |            I00017.sessionParameterKeys =               key(user.sessions.parameters)
       |            insert(I00017)
       |         }
       |      }
       |      user.sessions ⇒ {
       |
       |         post ⇒          {
       |          I00017.sessionParameterFrequency = 1
       |         }
       |      }
       |   }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames(frameName))
    assertLimits(r)
    found(r.rowSet) should equal(expected)

  }


  def found(rowSet: Array[FabricResultRow]): Array[_] = {
    rowSet.map {
      row => (row.cells(0).asLong, row.cells(1).asLong)
    } sortBy (_._1)
  }

  val expected: Array[Any] = Array(
    (12345, 1),
    (234567, 2),
    (345678, 3),
    (456789, 4)
  )

}
