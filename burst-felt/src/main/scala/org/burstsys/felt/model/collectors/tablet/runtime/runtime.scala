/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet

import org.burstsys.felt.model.collectors.runtime.FeltCollectorFactory

package object runtime {

  trait FeltTabletFactory extends Any with FeltCollectorFactory[FeltTabletBuilder, FeltTabletCollector]

}
