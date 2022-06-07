/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import org.burstsys.felt.model.collectors.decl.FeltCollectorProvider
import org.burstsys.felt.model.collectors.shrub.decl.{FeltShrubDecl, FeltShrubRef}

package object shrub {

  trait FeltShrubProvider
    extends FeltCollectorProvider[FeltShrubCollector, FeltShrubBuilder, FeltShrubRef, FeltShrubDecl, FeltShrubPlan]

}
