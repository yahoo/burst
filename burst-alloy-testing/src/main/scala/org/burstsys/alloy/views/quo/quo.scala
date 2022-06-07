/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.views

import org.burstsys.alloy.AlloyDatasetSpec
import org.burstsys.brio.model.schema.BrioSchema

package object quo {
  lazy val quoSchema: BrioSchema = BrioSchema("quo")

  lazy val over_quo_canned: AlloyDatasetSpec = AlloyDatasetSpec(quoSchema, 1L)
}
