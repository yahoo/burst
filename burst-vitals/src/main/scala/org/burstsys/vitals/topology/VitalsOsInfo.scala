/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.topology

/**
  * helper types/functions for OS information
  */
trait VitalsOsInfo {
  def osName: String = {
    System.getProperty("os.name", "unknown")
  }

  def platform(): String = {
    val osname = System.getProperty("os.name", "generic").toLowerCase
    if (osname.startsWith("windows")) {
      "win32"
    }
    else if (osname.startsWith("linux")) {
      "linux"
    }
    else if (osname.startsWith("sunos")) {
      "solaris"
    }
    else if (osname.startsWith("mac") || osname.startsWith("darwin")) {
      "mac"
    }
    else "generic"
  }

  def isWindows: Boolean = {
    osName.toLowerCase.indexOf("windows") >= 0
  }

  def isLinux: Boolean = {
    osName.toLowerCase.indexOf("linux") >= 0
  }

  def isUnix: Boolean = {
    val os = osName.toLowerCase

    // XXX: this obviously needs some more work to be "true" in general (see bottom of file)
    if ((os.indexOf("sunos") >= 0) || (os.indexOf("linux") >= 0)) {
      true
    }

    if (isMac && System.getProperty("os.version", "").startsWith("10.")) {
      true
    }

    false
  }

  def isMac: Boolean = {
    val os = osName.toLowerCase
    os.startsWith("mac") || os.startsWith("darwin")
  }

  def isSolaris: Boolean = {
    val os = osName.toLowerCase
    os.indexOf("sunos") >= 0
  }
}
