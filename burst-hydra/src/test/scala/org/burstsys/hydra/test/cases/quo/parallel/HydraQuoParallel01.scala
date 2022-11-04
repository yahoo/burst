/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.parallel

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoParallel01 extends HydraUseCase(1, 2, "quo") {

  //  override val sweep: HydraSweep = new B6BE2A8E9F3034A88808DB8B7CF70FB8C

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |
       |   query I00000 {
       |     cube user {
       |         limit = 1
       |         aggregates {
       |            eventCount:sum[long]
       |            sessionCount:sum[long]
       |            maxSessionTime:max[long]
       |            userCount:sum[long]
       |         }
       |      }
       |     user => {
       |
       |         post => {
       |            I00000.userCount = 1
       |         }
       |      }
       |      user.sessions => {
       |
       |         post => {
       |            I00000.sessionCount = 1
       |            I00000.maxSessionTime = user.sessions.startTime
       |         }
       |      }
       |      user.sessions.events => {
       |
       |         post => {
       |            I00000.eventCount = 1
       |         }
       |      }
       |   }
       |   query I00001 {
       |     cube user {
       |         limit = 1000
       |         aggregates {
       |            users:top[long](1000)
       |         }
       |         dimensions {
       |            deviceModelIds:verbatim[long]
       |         }
       |      }
       |     user => {
       |         pre => {
       |            I00001.deviceModelIds = user.deviceModelId
       |         }
       |         post => {
       |            I00001.users = 1
       |         }
       |      }
       |   }
       |   query I00002 {
       |     cube user {
       |        limit = 400
       |         aggregates {
       |            sessions:top[long](400)
       |         }
       |         dimensions {
       |            osVersionIds:verbatim[long]
       |         }
       |      }
       |     user.sessions => {
       |         pre => {
       |            I00002.osVersionIds = user.sessions.osVersion
       |         }
       |         post => {
       |            I00002.sessions = 1
       |         }
       |      }
       |   }
       |   query I00003 {
       |     cube user {
       |        limit = 400
       |         aggregates {
       |            projects:top[long](400)
       |         }
       |         cube user.project {
       |            dimensions {
       |               languageIds:verbatim[long]
       |            }
       |         }
       |      }
       |     user.project => {
       |         pre => {
       |            I00003.languageIds = user.project.languageId
       |         }
       |         post => {
       |            insert(I00003)
       |         }
       |      }
       |      user => {
       |
       |         post => {
       |            I00003.projects = 1
       |         }
       |      }
       |   }
       |   query I00004 {
       |     cube user {
       |         limit = 1000
       |         aggregates {
       |            users:top[long](1000)
       |         }
       |         cube user.sessions {
       |            dimensions {
       |               appVersionIds:verbatim[long]
       |            }
       |         }
       |      }
       |     user => {
       |         post => {
       |            I00004.users = 1
       |         }
       |      }
       |      user.sessions => {
       |         pre => {
       |            I00004.appVersionIds = user.sessions.appVersionId
       |         }
       |         post => {
       |            insert(I00004)
       |         }
       |      }
       |   }
       |   query I00005 {
       |     cube user {
       |         limit = 100
       |         aggregates {
       |            sessions:top[long](100)
       |         }
       |         dimensions {
       |            providedOrigins:verbatim[string]
       |         }
       |      }
       |     user.sessions => {
       |         pre => {
       |            I00005.providedOrigins = user.sessions.providedOrigin
       |         }
       |         post => {
       |            I00005.sessions = 1
       |         }
       |      }
       |   }
       |   query I00006 {
       |     cube user {
       |         limit = 50
       |         aggregates {
       |            sessions:top[long](50)
       |         }
       |         dimensions {
       |            mappedOrigin:verbatim[long]
       |         }
       |      }
       |     user.sessions => {
       |         pre => {
       |            I00006.mappedOrigin = user.sessions.mappedOrigin
       |         }
       |         post => {
       |            I00006.sessions = 1
       |         }
       |      }
       |   }
       |   query I00007 {
       |     cube user {
       |         limit = 10
       |         aggregates {
       |            sessions:top[long](10)
       |         }
       |         dimensions {
       |            originSourceType:verbatim[long]
       |         }
       |      }
       |     user.sessions => {
       |         pre => {
       |            I00007.originSourceType = user.sessions.originSourceType
       |         }
       |         post => {
       |            I00007.sessions = 1
       |         }
       |      }
       |   }
       |   query I00008 {
       |     cube user {
       |         limit = 10
       |         aggregates {
       |            sessions:top[long](10)
       |         }
       |         dimensions {
       |            originMethodType:verbatim[long]
       |         }
       |      }
       |     user.sessions => {
       |         pre => {
       |            I00008.originMethodType = user.sessions.originMethodType
       |         }
       |         post => {
       |            I00008.sessions = 1
       |         }
       |      }
       |   }
       |   query I00009 {
       |     cube user {
       |         limit = 1400
       |         aggregates {
       |            eventFrequency:top[long](1400)
       |         }
       |         dimensions {
       |            events:verbatim[long]
       |         }
       |      }
       |     user.sessions.events => {
       |         pre => {
       |            I00009.events = user.sessions.events.eventId
       |         }
       |         post => {
       |            I00009.eventFrequency = 1
       |         }
       |      }
       |   }
       |   query I00010 {
       |     cube user {
       |         limit = 14000
       |         aggregates {
       |            eventParameterFrequency:top[long](1400)
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
       |     user.sessions.events.parameters => {
       |         situ => {
       |               I00010.eventParameterKey = key(user.sessions.events.parameters)
       |               insert(I00010)
       |         }
       |      }
       |      user.sessions.events => {
       |         pre => {
       |            I00010.eventParameters = user.sessions.events.eventId
       |         }
       |         post => {
       |            I00010.eventParameterFrequency = 1
       |         }
       |      }
       |   }
       |   query I00011 {
       |     cube user {
       |         limit = 2000
       |         aggregates {
       |            campaignIdFrequency:top[long](2000)
       |         }
       |         cube user.channels {
       |            dimensions {
       |               campaignId:verbatim[long]
       |            }
       |         }
       |      }
       |     user.channels => {
       |         pre => {
       |            I00011.campaignId = user.channels.channelId
       |         }
       |         post => {
       |            insert(I00011)
       |         }
       |      }
       |      user => {
       |         post => {
       |            I00011.campaignIdFrequency = 1
       |         }
       |      }
       |   }
       |   query I00012 {
       |     cube user {
       |         limit = 200
       |         aggregates {
       |            channelIdFrequency:top[long](200)
       |         }
       |         cube user.channels {
       |            dimensions {
       |               channelId:verbatim[long]
       |            }
       |         }
       |      }
       |     user.channels => {
       |         pre => {
       |            I00012.channelId = user.channels.networkId
       |         }
       |         post => {
       |            insert(I00012)
       |         }
       |      }
       |      user => {
       |         post => {
       |            I00012.channelIdFrequency = 1
       |         }
       |      }
       |   }
       |   query I00013 {
       |     cube user {
       |         limit = 200
       |         aggregates {
       |            sessionParameterFrequency:top[long](200)
       |         }
       |         cube user.sessions.parameters {
       |            dimensions {
       |               sessionParameterKeys:verbatim[string]
       |            }
       |         }
       |      }
       |     user.sessions.parameters => {
       |         situ => {
       |               I00013.sessionParameterKeys = key(user.sessions.parameters)
       |               insert(I00013)
       |         }
       |      }
       |      user.sessions => {
       |
       |         post => {
       |            I00013.sessionParameterFrequency = 1
       |         }
       |      }
       |
       |   }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames("I00003"))
    assertLimits(r)
    /*
        val v: Long = r(0)[Long]("minSessionTime")
        v should not equal (0)
    */

  }


}
