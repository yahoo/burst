/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate.test.model

import org.burstsys.relate.{RelateEntity, RelatePk}

case class BurstSqlTestEntity(
                               var pk: RelatePk = 0L,
                               var test1: String
                             ) extends RelateEntity
