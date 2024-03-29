/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.sysinfo

import org.burstsys.vitals.properties.VitalsPropertyMap

trait SystemInfoComponent {
  /**
   * @return name of component
   */
  def name: String

  /**
   * System info about component.
   * @return Case class that will be serialized to Json
   */
  def status(level: Int, attributes: VitalsPropertyMap): Object

}
