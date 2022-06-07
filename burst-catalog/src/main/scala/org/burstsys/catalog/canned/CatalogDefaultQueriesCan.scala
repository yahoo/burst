/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.catalog.canned

import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType.Eql
import org.burstsys.catalog.api.BurstCatalogApiQueryLanguageType.Hydra
import org.burstsys.catalog.model.query.CatalogCannedQuery

final class CatalogDefaultQueriesCan extends CatalogCan {


  override def queries: Array[CatalogCannedQuery] = {
    Array(
      CatalogCannedQuery(
        "EQL count of users, sessions, events",
        Eql,
        s"""
           | select count(user) as users, count(user.sessions) as sessions, count(user.sessions.events) as events
           | from schema unity
        """.stripMargin
      ),
      CatalogCannedQuery(
        "Hydra count Users, Sessions, Events, Event Parameters",
        Hydra,
        s"""hydra SimpleObjectCount() {
           |    schema unity
           |    query default {
           |      cube user {
           |        limit 1
           |        aggregate {
           |          userCount:sum[long]
           |          sessionCount:sum[long]
           |          eventCount:sum[long]
           |          eventParameterCount:sum[long]
           |      }
           |    }
           |
           |    user ⇒ {
           |        pre ⇒ {
           |          default.userCount = 1
           |        }
           |    }
           |
           |    user.sessions ⇒ {
           |       pre ⇒ {
           |          default.sessionCount = 1
           |        }
           |    }
           |
           |    user.sessions.events ⇒ {
           |      pre ⇒ {
           |          default.eventCount = 1
           |      }
           |    }
           |
           |    user.sessions.events.parameters ⇒ {
           |      situ  ⇒ {
           |          default.eventParameterCount = 1
           |        }
           |      }
           |    }
           |}""".stripMargin
      )
    )
  }
}
