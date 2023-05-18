/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.quo.parallel

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraQuoParallel00 extends HydraUseCase(1, 1, "quo") {

  //  override val sweep: HydraSweep = new B6BE2A8E9F3034A88808DB8B7CF70FB8C

  override def analysisSource: String =
    s"""
       |hydra $analysisName() {
       |   schema 'quo'
       |   frame I00000 {
       |      cube user {
       |         limit = 1
       |         aggregates {
       |            userCount:sum[long]
       |         }
       |      }
       |      user => {
       |
       |         post =>          {
       |           $analysisName.I00000.userCount = 1
       |         }
       |      }
       |   }
       |   frame I00001 {
       |      cube I00001:user {
       |         limit = 1
       |         aggregates {
       |            sessionCount:sum[long]
       |         }
       |      }
       |      user.sessions => {
       |
       |         post =>          {
       |            $analysisName.I00001.sessionCount = 1
       |         }
       |      }
       |   }
       |   frame I00002 {
       |      cube I00002:user {
       |         limit = 1
       |         aggregates {
       |            eventCount:sum[long]
       |         }
       |      }
       |      user.sessions.events => {
       |
       |         post =>          {
       |            $analysisName.I00002.eventCount = 1
       |         }
       |      }
       |   }
       |   frame I00003 {
       |      cube I00003:user {
       |         limit = 1
       |         aggregates {
       |            minSessionTime:min[long]
       |         }
       |      }
       |      user.sessions => {
       |
       |         post =>          {
       |                        $analysisName.I00003.minSessionTime = user.sessions.startTime
       |         }
       |      }
       |   }
       |   frame I00004 {
       |      cube  user {
       |         limit = 1
       |         aggregates {
       |            maxSessionTime:max[long]
       |         }
       |      }
       |      user.sessions => {
       |
       |         post =>          {
       |                        I00004.maxSessionTime = user.sessions.startTime
       |         }
       |      }
       |   }
       |   frame I00005 {
       |      cube  user {
       |         limit = 1000
       |         aggregates {
       |            users:sum[long]
       |         }
       |         dimensions {
       |            deviceModelIds:verbatim[long]
       |         }
       |      }
       |      user => {
       |
       |         pre =>          {
       |                        $analysisName.I00005.deviceModelIds = user.deviceModelId
       |         }
       |         post =>          {
       |                        $analysisName.I00005.users = 1
       |         }
       |      }
       |   }
       |   frame I00006 {
       |      cube I00006:user {
       |        limit = 400
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            osVersionIds:verbatim[long]
       |         }
       |      }
       |      user.sessions => {
       |
       |         pre =>          {
       |                        $analysisName.I00006.osVersionIds = user.sessions.osVersion
       |         }
       |         post =>          {
       |                        $analysisName.I00006.sessions = 1
       |         }
       |      }
       |   }
       |   frame I00007 {
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
       |      user.project => {
       |
       |         pre =>          {
       |                        $analysisName.I00007.languageIds = user.project.languageId
       |         }
       |         post =>          {
       |                        insert($analysisName.I00007)
       |         }
       |      }
       |      user => {
       |
       |         post =>          {
       |                        $analysisName.I00007.projects = 1
       |         }
       |      }
       |   }
       |   frame I00008 {
       |      cube  user {
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
       |      user => {
       |
       |         post =>          {
       |                        $analysisName.I00008.users = 1
       |         }
       |      }
       |      user.sessions => {
       |
       |         pre =>          {
       |                        $analysisName.I00008.appVersionIds = user.sessions.appVersionId
       |         }
       |         post =>          {
       |                        insert($analysisName.I00008)
       |         }
       |      }
       |   }
       |   frame I00009 {
       |      cube user {
       |         limit = 100
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            providedOrigins:verbatim[string]
       |         }
       |      }
       |      user.sessions => {
       |
       |         pre =>          {
       |                        $analysisName.I00009.providedOrigins = user.sessions.providedOrigin
       |         }
       |         post =>          {
       |                        $analysisName.I00009.sessions = 1
       |         }
       |      }
       |   }
       |   frame I00010 {
       |      cube  user {
       |         limit = 50
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            mappedOrigin:verbatim[long]
       |         }
       |      }
       |      user.sessions => {
       |
       |         pre =>          {
       |                        $analysisName.I00010.mappedOrigin = user.sessions.mappedOrigin
       |         }
       |         post =>          {
       |                        $analysisName.I00010.sessions = 1
       |         }
       |      }
       |   }
       |   frame I00011 {
       |      cube  user {
       |         limit = 10
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            originSourceType:verbatim[long]
       |         }
       |      }
       |      user.sessions => {
       |
       |         pre =>          {
       |          $analysisName.I00011.originSourceType = user.sessions.originSourceType
       |         }
       |         post =>          {
       |                        $analysisName.I00011.sessions = 1
       |         }
       |      }
       |   }
       |   frame I00012 {
       |      cube  user {
       |         limit = 10
       |         aggregates {
       |            sessions:sum[long]
       |         }
       |         dimensions {
       |            originMethodType:verbatim[long]
       |         }
       |      }
       |      user.sessions => {
       |
       |         pre =>          {
       |            $analysisName.I00012.originMethodType = user.sessions.originMethodType
       |         }
       |         post =>          {
       |            $analysisName.I00012.sessions = 1
       |         }
       |      }
       |   }
       |   frame I00013 {
       |      cube  user {
       |         limit = 1400
       |         aggregates {
       |            eventFrequency:sum[long]
       |         }
       |         dimensions {
       |            events:verbatim[long]
       |         }
       |      }
       |      user.sessions.events => {
       |
       |         pre =>          {
       |            $analysisName.I00013.events = user.sessions.events.eventId
       |         }
       |         post =>          {
       |            $analysisName.I00013.eventFrequency = 1
       |         }
       |      }
       |   }
       |   frame I00014 {
       |      cube  user {
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
       |      user.sessions.events.parameters => {
       |
       |         situ =>          {
       |            $analysisName.I00014.eventParameterKey = key(user.sessions.events.parameters)
       |            insert($analysisName.I00014)
       |         }
       |      }
       |      user.sessions.events => {
       |
       |         pre =>          {
       |            $analysisName.I00014.eventParameters = user.sessions.events.eventId
       |         }
       |         post =>          {
       |            $analysisName.I00014.eventParameterFrequency = 1
       |         }
       |      }
       |   }
       |   frame I00015 {
       |      cube  user {
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
       |      user.channels => {
       |
       |         pre =>          {
       |                        $analysisName.I00015.campaignId = user.channels.channelId
       |         }
       |         post =>          {
       |                        insert($analysisName.I00015)
       |         }
       |      }
       |      user => {
       |
       |         post =>          {
       |                        $analysisName.I00015.campaignIdFrequency = 1
       |         }
       |      }
       |   }
       |   frame I00016 {
       |      cube  user {
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
       |      user.channels => {
       |
       |         pre =>          {
       |                        $analysisName.I00016.channelId = user.channels.networkId
       |         }
       |         post =>          {
       |            insert($analysisName.I00016)
       |         }
       |      }
       |      user => {
       |
       |         post =>          {
       |            $analysisName.I00016.channelIdFrequency = 1
       |         }
       |      }
       |   }
       |   frame I00017 {
       |      cube  user {
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
       |      user.sessions.parameters => {
       |
       |         situ =>          {
       |            $analysisName.I00017.sessionParameterKeys =               key(user.sessions.parameters)
       |            insert($analysisName.I00017)
       |         }
       |      }
       |      user.sessions => {
       |
       |         post =>          {
       |          $analysisName.I00017.sessionParameterFrequency = 1
       |         }
       |      }
       |   }
       |}
       """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    val r = result.resultSets(result.resultSetNames("I00003"))
    assertLimits(r)
    val v: Long = r(0)[Long]("minSessionTime")
    v should not equal (0)

  }


}
