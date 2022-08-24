![Burst](../../doc/burst_small.png "")

_Motif:_ ```'recurring salient thematic element...'```

# JSON Support

## JSON Schema

```json 
{  "SCHEMA" : {
        "schemaName" : "Quo",
        "rootFieldName" : "user",
        "rootStructureName" : "User",
        "structures" : {
          "Project" : {
            "STRUCT" : {
              "name" : "Project",
              "relations" : {
                "gender" : {
                  "VAL_SCAL" : {
                    "vtype" : "BYTE",
                    "fnum" : 11,
                    "fname" : "gender"
                  }
                },
                "regionId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 7,
                    "fname" : "regionId"
                  }
                },
                "retainedTime" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 3,
                    "fname" : "retainedTime"
                  }
                },
                "stateId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 5,
                    "fname" : "stateId"
                  }
                },
                "languageId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 9,
                    "fname" : "languageId"
                  }
                },
                "installTime" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "ordinal" ],
                    "vtype" : "LONG",
                    "fnum" : 1,
                    "fname" : "installTime"
                  }
                },
                "cityId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 4,
                    "fname" : "cityId"
                  }
                },
                "lastUsedTime" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 2,
                    "fname" : "lastUsedTime"
                  }
                },
                "projectId" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "key" ],
                    "vtype" : "LONG",
                    "fnum" : 0,
                    "fname" : "projectId"
                  }
                },
                "birthDate" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 10,
                    "fname" : "birthDate"
                  }
                },
                "countryId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 6,
                    "fname" : "countryId"
                  }
                },
                "localeId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 8,
                    "fname" : "localeId"
                  }
                }
              }
            }
          },
          "User" : {
            "STRUCT" : {
              "name" : "User",
              "relations" : {
                "sessions" : {
                  "REF_VEC" : {
                    "fnum" : 2,
                    "fname" : "sessions"
                  }
                },
                "channels" : {
                  "REF_VEC" : {
                    "fnum" : 4,
                    "fname" : "channels"
                  }
                },
                "deviceModelId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 6,
                    "fname" : "deviceModelId"
                  }
                },
                "project" : {
                  "REF_SCAL" : {
                    "fnum" : 1,
                    "fname" : "project"
                  }
                },
                "personas" : {
                  "REF_VEC" : {
                    "fnum" : 5,
                    "fname" : "personas"
                  }
                },
                "parameters" : {
                  "VAL_MAP" : {
                    "vtype" : "STRING",
                    "ktype" : "STRING",
                    "fnum" : 8,
                    "fname" : "parameters"
                  }
                },
                "flurryId" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "key" ],
                    "vtype" : "STRING",
                    "fnum" : 0,
                    "fname" : "flurryId"
                  }
                },
                "segments" : {
                  "REF_VEC" : {
                    "fnum" : 3,
                    "fname" : "segments"
                  }
                },
                "deviceSubModelId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 7,
                    "fname" : "deviceSubModelId"
                  }
                }
              }
            }
          },
          "Persona" : {
            "STRUCT" : {
              "name" : "Persona",
              "relations" : {
                "personaId" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "key" ],
                    "vtype" : "LONG",
                    "fnum" : 0,
                    "fname" : "personaId"
                  }
                }
              }
            }
          },
          "Channel" : {
            "STRUCT" : {
              "name" : "Channel",
              "relations" : {
                "isQuality" : {
                  "VAL_SCAL" : {
                    "vtype" : "BOOLEAN",
                    "fnum" : 2,
                    "fname" : "isQuality"
                  }
                },
                "networkId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 1,
                    "fname" : "networkId"
                  }
                },
                "channelId" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "key" ],
                    "vtype" : "LONG",
                    "fnum" : 0,
                    "fname" : "channelId"
                  }
                }
              }
            }
          },
          "Event" : {
            "STRUCT" : {
              "name" : "Event",
              "relations" : {
                "duration" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 2,
                    "fname" : "duration"
                  }
                },
                "eventId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 0,
                    "fname" : "eventId"
                  }
                },
                "startTime" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "key", "ordinal" ],
                    "vtype" : "LONG",
                    "fnum" : 1,
                    "fname" : "startTime"
                  }
                },
                "eventType" : {
                  "VAL_SCAL" : {
                    "vtype" : "BYTE",
                    "fnum" : 4,
                    "fname" : "eventType"
                  }
                },
                "parameters" : {
                  "VAL_MAP" : {
                    "vtype" : "STRING",
                    "ktype" : "STRING",
                    "fnum" : 5,
                    "fname" : "parameters"
                  }
                },
                "order" : {
                  "VAL_SCAL" : {
                    "vtype" : "INTEGER",
                    "fnum" : 3,
                    "fname" : "order"
                  }
                }
              }
            }
          },
          "Segment" : {
            "STRUCT" : {
              "name" : "Segment",
              "relations" : {
                "segmentId" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "key" ],
                    "vtype" : "LONG",
                    "fnum" : 0,
                    "fname" : "segmentId"
                  }
                }
              }
            }
          },
          "Session" : {
            "STRUCT" : {
              "name" : "Session",
              "relations" : {
                "appVersionId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 12,
                    "fname" : "appVersionId"
                  }
                },
                "originSourceType" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 18,
                    "fname" : "originSourceType"
                  }
                },
                "genderReported" : {
                  "VAL_SCAL" : {
                    "vtype" : "BYTE",
                    "fnum" : 22,
                    "fname" : "genderReported"
                  }
                },
                "timeZoneId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 4,
                    "fname" : "timeZoneId"
                  }
                },
                "totalEvents" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 14,
                    "fname" : "totalEvents"
                  }
                },
                "totalErrors" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 3,
                    "fname" : "totalErrors"
                  }
                },
                "stateId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 8,
                    "fname" : "stateId"
                  }
                },
                "sessionId" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "key" ],
                    "vtype" : "LONG",
                    "fnum" : 0,
                    "fname" : "sessionId"
                  }
                },
                "cityId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 9,
                    "fname" : "cityId"
                  }
                },
                "countryId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 7,
                    "fname" : "countryId"
                  }
                },
                "birthDateReported" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 21,
                    "fname" : "birthDateReported"
                  }
                },
                "duration" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 13,
                    "fname" : "duration"
                  }
                },
                "providedOrigin" : {
                  "VAL_SCAL" : {
                    "vtype" : "STRING",
                    "fnum" : 16,
                    "fname" : "providedOrigin"
                  }
                },
                "mappedOrigin" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 17,
                    "fname" : "mappedOrigin"
                  }
                },
                "osVersion" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 1,
                    "fname" : "osVersion"
                  }
                },
                "regionId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 6,
                    "fname" : "regionId"
                  }
                },
                "agentVersionId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 11,
                    "fname" : "agentVersionId"
                  }
                },
                "startTime" : {
                  "VAL_SCAL" : {
                    "classifiers" : [ "ordinal" ],
                    "vtype" : "LONG",
                    "fnum" : 2,
                    "fname" : "startTime"
                  }
                },
                "carrierId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 10,
                    "fname" : "carrierId"
                  }
                },
                "parameters" : {
                  "VAL_MAP" : {
                    "vtype" : "STRING",
                    "ktype" : "STRING",
                    "fnum" : 20,
                    "fname" : "parameters"
                  }
                },
                "localeId" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 5,
                    "fname" : "localeId"
                  }
                },
                "events" : {
                  "REF_VEC" : {
                    "fnum" : 15,
                    "fname" : "events"
                  }
                },
                "originMethodType" : {
                  "VAL_SCAL" : {
                    "vtype" : "LONG",
                    "fnum" : 19,
                    "fname" : "originMethodType"
                  }
                }
              }
            }
          }
        }
      }
    }
}
```
