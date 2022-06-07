/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy

import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties.VitalsPropertyKey

package object store extends VitalsLogger {

  /**
   * This property is used to find a json file (possibly gzipped) that contains data to load into a view.
   */
  final val AlloyViewDataPathProperty: VitalsPropertyKey = "burst.view.path"

}
