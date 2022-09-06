/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.canned

import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType.Hydra
import org.burstsys.catalog.canned.CatalogCan
import org.burstsys.catalog.model.query.CatalogCannedQuery

/**
  * canned metadata for hydra module
  */
final class HydraQueriesCan extends CatalogCan {

  override def queries: Array[CatalogCannedQuery] = {
    Array(
      CatalogCannedQuery(
        "HydraUnityPar1",
        Hydra,
        s"""
        hydra HydraUnityPar1() {
          schema unity
          query languageIds {
            cube user {
              limit 5
              dimensions {
                languageId:verbatim[long]
              }
            }
            user.application.firstUse => {
              pre => {
                languageIds.languageId = user.application.firstUse.languageId
                insert(languageIds)
              }
            }
          }
          query appVersionIds {
            cube user {
              limit 6
              aggregates {
                frequency:sum[long]
              }
              dimensions {
                appVersion:verbatim[long]
              }
            }
            user.sessions => {
              pre => {
                appVersionIds.appVersion = user.sessions.appVersion.id
                appVersionIds.frequency = 1
              }
            }
          }
          query providedOrigins {
            cube user {
              limit 5
              aggregates {
                frequency:sum[long]
              }
              dimensions {
                providedOrigin:verbatim[string]
              }
            }
            user.sessions => {
              post => {
                providedOrigins.providedOrigin = user.sessions.providedOrigin
                providedOrigins.frequency = 1
              }
            }
          }
          query mappedOrigins {
            cube user {
              limit 4
              aggregates {
                frequency:sum[long]
              }
              dimensions {
                mappedOrigin:verbatim[long]
              }
            }
            user.sessions => {
              pre => {
                mappedOrigins.mappedOrigin = user.sessions.mappedOriginId
                mappedOrigins.frequency = 1
              }
            }
          }
          query originSourceTypes {
            cube user {
              limit 5
              aggregates {
                frequency:sum[long]
              }
              dimensions {
                originSourceTypeId:verbatim[long]
              }
            }
            user.sessions => {
              pre => {
                originSourceTypes.originSourceTypeId = user.sessions.originSourceTypeId
                originSourceTypes.frequency = 1
              }
            }
          }
          query sessionParameterKeys {
            cube user {
              limit 7
              cube user.sessions {
                aggregates {
                  parameterFrequency:sum[long]
                }
                dimensions {
                  parameterKey:verbatim[string]
                }
              }
            }
            user.sessions.parameters => {
              situ => {
                sessionParameterKeys.parameterKey = key(user.sessions.parameters)
                sessionParameterKeys.parameterFrequency = 1
              }
            }
          }
          query unityEventIdFrequencies {
            cube user {
              limit 11
              cube user.sessions.events {
                aggregates {
                  eventFrequency:sum[long]
                }
                dimensions {
                  eventId:verbatim[long]
                }
              }
            }
            user.sessions.events => {
              pre => {
                unityEventIdFrequencies.eventId = user.sessions.events.id
                unityEventIdFrequencies.eventFrequency = 1
              }
            }
          }
          query parameterKeyEventFrequencies  {
            cube user {
              limit 77
              cube user.sessions.events.parameters {
                aggregates {
                  parameterKeyFrequency:sum[long]
                }
                dimensions {
                  eventId:verbatim[long]
                  parameterKey:verbatim[string]
                }
              }
            }
            user.sessions.events.parameters => {
              situ => {
                parameterKeyEventFrequencies.parameterKey = key(user.sessions.events.parameters)
                parameterKeyEventFrequencies.eventId = user.sessions.events.id
                parameterKeyEventFrequencies.parameterKeyFrequency = 1
              }
            }
          }
          query localeCountryIds  {
            cube user {
              limit 6
              aggregates {
                userCount:sum[long]
              }
              cube user.application {
                dimensions {
                  localeCountryId:verbatim[long]
                }
              }
            }
            user => {
              pre => {
                localeCountryIds.userCount = 1
              }
            }
            user.application => {
              pre => {
                localeCountryIds.localeCountryId = user.application.firstUse.localeCountryId
                insert(localeCountryIds)
              }
            }
          }
          query eventParameterKeyFrequencies {
            cube user {
            limit 7
              aggregates {
                eventFrequency:sum[long]
              }
              cube user.sessions.events.parameters {
                dimensions {
                  parameterKey:verbatim[string]
                }
              }
            }
            user.sessions.events => {
              pre => {
                eventParameterKeyFrequencies.eventFrequency = 1
              }
            }
            user.sessions.events.parameters => {
              situ => {
                eventParameterKeyFrequencies.parameterKey = key(user.sessions.events.parameters)
                insert(eventParameterKeyFrequencies)
              }
            }
          }
        }
          """.stripMargin),

      CatalogCannedQuery(
        "HydraQuoPar1",
        Hydra,
        s"""
           |hydra HydraQuoPar1() {
           |  schema quo
           |  query query1 {
           |    cube user {
           |      limit 2
           |      cube user.sessions {
           |        aggregates {
           |          sessionCount:sum[long]
           |        }
           |        dimensions {
           |          appVersion:verbatim[long]
           |        }
           |      }
           |    }
           |    user.sessions => {
           |      pre => {
           |        query1.appVersion = user.sessions.appVersionId
           |        query1.sessionCount = 1
           |      }
           |    }
           |  }
           |  query query2 {
           |    cube user {
           |      limit 4
           |      cube user.sessions {
           |        aggregates {
           |          userCount:sum[long]
           |        }
           |        dimensions {
           |          deviceModel:verbatim[long]
           |        }
           |      }
           |    }
           |    user => {
           |      pre => {
           |        query2.deviceModel = user.deviceModelId
           |        query2.userCount = 1
           |      }
           |    }
           |  }
           |  query query3 {
           |    cube user {
           |      limit 15
           |      cube user.sessions.events {
           |     aggregates {
           |       eventCount:sum[long]
           |     }
           |     dimensions {
           |       eventId:verbatim[long]
           |     }
           |   }
           | }
           | user.sessions.events => {
           |    pre => {
           |      query3.eventId = user.sessions.events.eventId
           |      query3.eventCount = 1
           |    }
           | }
           |}
           |
           |query query4 {
           | cube user {
           |   limit 12
           |   cube user.sessions.events {
           |     aggregates {
           |       eventParameterFrequency:sum[long]
           |     }
           |     dimensions {
           |       eventId:verbatim[long]
           |       eventParameterKey:verbatim[string]
           |     }
           |   }
           | }
           | user.sessions.events.parameters => {
           |   situ => {
           |     query4.eventId = user.sessions.events.eventId
           |     query4.eventParameterKey = key(user.sessions.events.parameters)
           |     query4.eventParameterFrequency = 1
           |   }
           | }
           |}

           |query query5 {
           | cube user {
           |   limit 34
           |   cube user.sessions {
           |     aggregates {
           |       userCount:sum[long]
           |     }
           |     dimensions {
           |       deviceModel:verbatim[long]
           |     }
           |   }
           | }
           | user => {
           |   pre => {
           |     query5.deviceModel = user.deviceModelId
           |     query5.userCount = 1
           |   }
           | }
           |  }

           |query query6 {
           | cube user {
           | limit 2
           |   cube user.sessions {
           |     aggregates {
           |       sessionCount:sum[long]
           |     }
           |     dimensions {
           |       originSourceType:verbatim[long]
           |     }
           |   }
           | }
           | user.sessions => {
           |   pre => {
           |     query6.originSourceType = user.sessions.originSourceType
           |     query6.sessionCount = 1
           |   }
           | }
           |}

           |query query7 {
           | cube user {
           |   limit 2
           |   cube user.sessions {
           |     aggregates {
           |       sessionCount:sum[long]
           |     }
           |     dimensions {
           |       osVersion:verbatim[long]
           |     }
           |   }
           | }
           | user.sessions => {
           |   pre => {
           |     query7.osVersion = user.sessions.osVersion
           |     query7.sessionCount = 1
           |   }
           | }
           |}

           |query query8 {
           | cube user {
           |   limit 2
           |   cube user.sessions {
           |     aggregates {
           |       sessions:sum[long]
           |     }
           |     dimensions {
           |       providedOrigin:verbatim[string]
           |     }
           |   }
           | }
           | user.sessions => {
           |   pre => {
           |     query8.providedOrigin = user.sessions.providedOrigin
           |     query8.sessions = 1
           |   }
           | }
           |}

           |query query9 {
           | cube user {
           |   limit 1
           |   aggregates {
           |     userCount:sum[long]
           |     sessionCount:sum[long]
           |     eventCount:sum[long]
           |   }
           | }
           | user => {
           |   pre => {
           |     query9.userCount = 1
           |   }
           | }
           |  user.sessions => {
           |    pre => {
           |      query9.sessionCount = 1
           |    }
           |  }
           |            user.sessions.events => {
           |              pre => {
           |                query9.eventCount = 1
           |              }
           |            }
           |          }
           |
           |          query query10 {
           |            cube user {
           |              limit 1
           |              aggregates {
           |                userCount:sum[long]
           |                sessionCount:sum[long]
           |                eventCount:sum[long]
           |                eventParameterCount:sum[long]
           |              }
           |            }
           |            user => {
           |              pre => {
           |                query10.userCount = 1
           |              }
           |            }
           |            user.sessions => {
           |              pre => {
           |                query10.sessionCount = 1
           |              }
           |            }
           |            user.sessions.events => {
           |              pre => {
           |                query10.eventCount = 1
           |              }
           |            }
           |            user.sessions.events.parameters => {
           |              situ  => {
           |                query10.eventParameterCount = 1
           |              }
           |            }
           |          }
           |
           |          query query11 {
           |            cube user {
           |              limit 1
           |              aggregates {
           |                userCount0:sum[long]
           |                sessionCount0:sum[long]
           |                eventCount0:sum[long]
           |                eventParameterCount0:sum[long]
           |              }
           |            }
           |            user => {
           |              pre => {
           |                query11.userCount0 = 1
           |              }
           |            }
           |            user.sessions => {
           |              pre => {
           |                query11.sessionCount0 = 1
           |              }
           |            }
           |            user.sessions.events => {
           |              pre => {
           |                query11.eventCount0 = 1
           |              }
           |            }
           |            user.sessions.events.parameters => {
           |              situ  => {
           |                query11.eventParameterCount0 = 1
           |              }
           |            }
           |          }
           |
           |          query query12 {
           |            cube user {
           |              limit 7
           |              cube user.sessions {
           |                aggregates {
           |                  sessionCount:sum[long]
           |                }
           |                dimensions {
           |                  dow:dayOfWeek[long]
           |                }
           |              }
           |            }
           |            user.sessions => {
           |              pre => {
           |                query12.dow = user.sessions.startTime
           |                query12.sessionCount = 1
           |              }
           |            }
           |          }
           |
           |          query query13 {
           |            cube user {
           |              limit 404
           |              aggregates {
           |                sessionCount:sum[long]
           |              }
           |              dimensions {
           |                flurryId:verbatim[string]
           |              }
           |            }
           |            user => {
           |              pre => {
           |                query13.flurryId = user.flurryId
           |                query13.sessionCount = size(user.sessions)
           |              }
           |            }
           |          }
           |
           |          query query14 {
           |            cube user {
           |              limit 20
           |              aggregates {
           |                userCount:sum[long]
           |              }
           |              dimensions {
           |                gender:verbatim[byte]
           |              }
           |              cube user.segments {
           |                dimensions {
           |                  segmentId:verbatim[long]
           |                }
           |              }
           |            }
           |            user => {
           |              pre => {
           |                query14.gender = user.project.gender
           |                query14.userCount = 1
           |              }
           |            }
           |            user.segments => {
           |              pre => {
           |                query14.segmentId = user.segments.segmentId
           |                insert(query14)
           |              }
           |            }
           |          }
           |
           |          query query17 {
           |            cube user {
           |              limit 64
           |              cube user.sessions.events {
           |                aggregates {
           |                  eventCount:sum[long]
           |                }
           |              }
           |              cube user.personas {
           |                dimensions {
           |                  persona:verbatim[long]
           |                }
           |              }
           |            }
           |            user.personas => {
           |            post => {
           |              query17.persona = user.personas.personaId
           |                insert(query17)
           |              }
           |            }
           |            user.sessions.events => {
           |              post => {
           |                query17.eventCount = 1
           |              }
           |            }
           |          }
           |
           |        }""".stripMargin)
    )
  }


}
