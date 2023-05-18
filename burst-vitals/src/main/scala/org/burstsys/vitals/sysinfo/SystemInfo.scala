/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.sysinfo

trait SystemInfo {

  def registerComponent(toRegister: SystemInfoComponent*): Unit

  def deregisterComponent(toDeregister: SystemInfoComponent*): Unit

  def systemStatus(): Object

  def components: Iterator[SystemInfoComponent]
}
