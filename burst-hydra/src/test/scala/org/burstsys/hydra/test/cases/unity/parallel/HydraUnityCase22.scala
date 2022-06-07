/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.parallel

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.hydra.test.cases.support.HydraUseCase

object HydraUnityCase22 extends HydraUseCase(200, 200, "unity") {

  //      override val sweep: BurstHydraSweep = new BE65BEBF88AFD4AE18AEBF403F019E3BC

  override val frameSource: String =
    s"""
    frame frame1 {
            cube user {
                limit = 100
                aggregates {
                   userCount:sum[long]
                }
             }
            user ⇒ {
                post ⇒ {
                   $analysisName.frame1.userCount = 1
                }
             }
    }
    frame frame2 {
      cube user {
          limit = 100
          aggregates {
             sessionCount:sum[long]
          }
       }
      user.sessions ⇒ {
          post ⇒ {
             $analysisName.frame2.sessionCount = 1
          }
       }
    }
    frame frame3 {
      cube user {
          limit = 100
          aggregates {
             eventCount:sum[long]
          }
       }
      user.sessions.events ⇒ {
          post ⇒ {
             $analysisName.frame3.eventCount = 1
          }
       }
    }
    frame frame4 {
      cube user {
          limit = 100
          aggregates {
             minInstallTime:min[long]
          }
       }
      user.application.firstUse ⇒ {
          post ⇒ {
             $analysisName.frame4.minInstallTime = user.application.firstUse.sessionTime
          }
       }
    }
    frame frame5 {
      cube user {
          limit = 100
          aggregates {
             maxInstallTime:max[long]
          }
       }
      user.application.firstUse ⇒ {
          post ⇒ {
             $analysisName.frame5.maxInstallTime = user.application.firstUse.sessionTime
          }
       }
    }
    frame frame6 {
      cube user {
          limit = 100
          aggregates {
             mixInstallTime:min[long]
          }
       }
      user.sessions ⇒ {
          post ⇒ {
             $analysisName.frame6.mixInstallTime = user.sessions.startTime
          }
       }
    }
    frame frame7 {
      cube user {
          limit = 100
          aggregates {
             maxInstallTime:max[long]
          }
       }
      user.sessions ⇒ {
          post ⇒ {
             $analysisName.frame7.maxInstallTime = user.sessions.startTime
          }
       }
    }
    frame frame8 {
      cube user {
          limit = 100
          aggregates {
             users:sum[long]
          }
          dimensions {
             deviceModel:verbatim[long]
          }
       }
      user ⇒ {
          pre ⇒ {
             $analysisName.frame8.deviceModel = user.deviceModelId
          }
          post ⇒ {
             $analysisName.frame8.users = 1
          }
       }
    }
    frame frame9 {
        cube user {
            limit = 100
            aggregates {
               sessions:sum[long]
            }
            dimensions {
               osVersionId:verbatim[long]
            }
         }
        user.sessions ⇒ {
            pre ⇒ {
               $analysisName.frame9.osVersionId = user.sessions.osVersionId
            }
            post ⇒ {
               $analysisName.frame9.sessions = 1
            }
         }
    }
    frame frame10 {
            cube user {
                limit = 100
                aggregates {
                   projects:sum[long]
                }
                cube user.application {
                   dimensions {
                      languageId:verbatim[long]
                   }
                }
             }
            user ⇒ {
                post ⇒ {
                   $analysisName.frame10.projects = 1
                }
             }
             user.application.firstUse ⇒ {
                pre ⇒ {
                   $analysisName.frame10.languageId = user.application.firstUse.languageId
                }
             }
             user.application ⇒ {
                post ⇒ {
                   insert($analysisName.frame10)
                }
             }
    }
    frame frame11 {
            cube user {
                limit = 100
                aggregates {
                   users:sum[long]
                }
                cube user.sessions {
                   dimensions {
                      appVersion:verbatim[long]
                   }
                }
             }
            user.sessions.appVersion ⇒ {
                pre ⇒ {
                   $analysisName.frame11.appVersion = user.sessions.appVersion.id
                }
             }
             user ⇒ {
                post ⇒ {
                   $analysisName.frame11.users = 1
                }
             }
             user.sessions ⇒ {
                post ⇒ {
                   insert($analysisName.frame11)
                }
             }
    }
    frame frame12 {
            cube user {
                limit = 100
                aggregates {
                   sessions:sum[long]
                }
                dimensions {
                   providedOrigin:verbatim[string]
                }
             }
            user.sessions ⇒ {
                pre ⇒ {
                   $analysisName.frame12.providedOrigin = user.sessions.providedOrigin
                }
                post ⇒ {
                   $analysisName.frame12.sessions = 1
                }
             }
    }
    frame frame13 {
            cube user {
                limit = 100
                aggregates {
                   sessions:sum[long]
                }
                dimensions {
                   mappedOrigin:verbatim[long]
                }
             }
            user.sessions ⇒ {
                pre ⇒ {
                   $analysisName.frame13.mappedOrigin = user.sessions.mappedOriginId
                }
                post ⇒ {
                   $analysisName.frame13.sessions = 1
                }
             }
          }
    frame frame14 {
            cube user {
                limit = 100
                aggregates {
                   sessions:sum[long]
                }
                dimensions {
                   originSourceType:verbatim[long]
                }
             }
            user.sessions ⇒ {
                pre ⇒ {
                   $analysisName.frame14.originSourceType = user.sessions.originSourceTypeId
                }
                post ⇒ {
                   $analysisName.frame14.sessions = 1
                }
             }
    }
    frame frame15 {
            cube user {
                limit = 100
                aggregates {
                   sessions:sum[long]
                }
                dimensions {
                   originMethodType:verbatim[long]
                }
             }
            user.sessions ⇒ {
                pre ⇒ {
                   $analysisName.frame15.originMethodType = user.sessions.originMethodTypeId
                }
                post ⇒ {
                   $analysisName.frame15.sessions = 1
                }
            }
    }
    frame frame16 {
            cube user {
                limit = 100
                aggregates {
                   eventFrequency:sum[long]
                }
                dimensions {
                   eventId:verbatim[long]
                }
             }
            user.sessions.events ⇒ {
                pre ⇒ {
                   $analysisName.frame16.eventId = user.sessions.events.id
                }
                post ⇒ {
                   $analysisName.frame16.eventFrequency = 1
                }
             }
    }
    frame frame17 {
            cube user {
                limit = 100
                aggregates {
                   localeIdFrequency:sum[long]
                }
                dimensions {
                   localeId:verbatim[long]
                }
             }
            user.sessions ⇒ {
                pre ⇒ {
                   $analysisName.frame17.localeId = user.sessions.localeId
                }
                post ⇒ {
                   $analysisName.frame17.localeIdFrequency = 1
                }
             }
    }
    frame frame18 {
            cube user {
                limit = 100
                aggregates {
                   variantIdFrequency:sum[long]
                }
                dimensions {
                   variantId:verbatim[long]
                }
             }
            user.sessions.variants ⇒ {
                pre ⇒ {
                   $analysisName.frame18.variantId = user.sessions.variants.id
                }
                post ⇒ {
                   $analysisName.frame18.variantIdFrequency = 1
                }
             }
    }
    frame frame19 {
            cube user {
                limit = 100
                aggregates {
                   campaignIdFrequency:sum[long]
                }
                cube user.application {
                   dimensions {
                      campaignId:verbatim[long]
                   }
                }
             }
            user ⇒ {
                post ⇒ {
                   $analysisName.frame19.campaignIdFrequency = 1
                }
             }
             user.application.channels ⇒ {
                pre ⇒ {
                   $analysisName.frame19.campaignId = user.application.channels.campaignId
                }
             }
             user.application ⇒ {
                post ⇒ {
                   insert($analysisName.frame19)
                }
             }
    }
    frame frame20 {
            cube user {
                limit = 100
                aggregates {
                   channelIdFrequency:sum[long]
                }
                cube user.application {
                   dimensions {
                      channelId:verbatim[long]
                   }
                }
             }
            user ⇒ {
                post ⇒ {
                   $analysisName.frame20.channelIdFrequency = 1
                }
             }
             user.application.channels ⇒ {
                pre ⇒ {
                   $analysisName.frame20.channelId = user.application.channels.channelId
                }
             }
             user.application ⇒ {
                post ⇒ {
                   insert($analysisName.frame20)
                }
             }
          }
    frame frame21 {
            cube user {
                limit = 100
                aggregates {
                   sessionParameterFrequency:sum[long]
                }
                cube user.sessions.parameters {
                   dimensions {
                      sessionParameterKey:verbatim[string]
                   }
                }
             }
            user.sessions.parameters ⇒ {
                situ ⇒ {
                   $analysisName.frame21.sessionParameterKey = key(user.sessions.parameters)
                   insert($analysisName.frame21)
                }
             }
             user.sessions ⇒ {
                post ⇒ {
                   $analysisName.frame21.sessionParameterFrequency = 1
                }
             }
          }
    frame frame22 {
            cube user {
                limit = 100
                aggregates {
                   eventParameterFrequency:sum[long]
                }
                dimensions {
                   eventId:verbatim[long]
                }
                cube user.sessions.events.parameters {
                   dimensions {
                      eventParameterKey:verbatim[string]
                   }
                }
             }
            user.sessions.events.parameters ⇒ {
                situ ⇒ {
                   $analysisName.frame22.eventParameterKey = key(user.sessions.events.parameters)
                   insert($analysisName.frame22)
                }
             }
             user.sessions.events ⇒ {
                pre ⇒ {
                   $analysisName.frame22.eventId = user.sessions.events.id
                }
                post ⇒ {
                   $analysisName.frame22.eventParameterFrequency = 1
                }
             }
    }
        """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {
    //result
  }


}
