/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route

import org.burstsys.felt.model.collectors.runtime.FeltCollectorFactory
import org.burstsys.vitals.io.MB

package object runtime {

  final val FeltRouteDefaultSize = 10 * MB.toInt

  trait FeltRouteFactory extends Any with FeltCollectorFactory[FeltRouteBuilder, FeltRouteCollector]

}
