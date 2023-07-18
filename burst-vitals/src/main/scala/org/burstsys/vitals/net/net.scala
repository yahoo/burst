/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals

import java.net._

import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging._

import scala.language.implicitConversions

/**
  * helper types/functions for network
  */
package object net extends VitalsLogger {

  type VitalsHostName = String
  type VitalsHostAddress = String
  type VitalsUrl = String
  type VitalsHostPort = Int

  def fqdnToShortForm(host: VitalsHostAddress): VitalsHostAddress = {
    val index = host.indexOf('.')
    if (index != -1) host.substring(0, index) else host
  }

  def isIpv4Address(address: VitalsHostAddress): Boolean = {
    val components = address.split(".")
    if (components.length != 4) return false
    components foreach {
      c =>
        try {
          c.toInt
        } catch safely {
          case t: NumberFormatException =>
            return false
        }
    }
    true
  }

  def convertLocalHostnameToExternal(hostname: VitalsHostName): VitalsHostName = {
    if (!hostname.equals("localhost")) hostname
    else {
      InetAddress.getLocalHost.getHostName
    }
  }

  def convertLocalAddressToExternal(ipaddress: VitalsHostAddress): VitalsHostAddress = {
    if (!ipaddress.equals("127.0.0.1"))
      ipaddress
    else {
      val ifaces = NetworkInterface.getNetworkInterfaces
      while (ifaces.hasMoreElements) {
        val iface = ifaces.nextElement()
        val addresses = iface.getInetAddresses
        while (addresses.hasMoreElements) {
          val addr = addresses.nextElement
          if (addr.isInstanceOf[Inet4Address] && !addr.isLoopbackAddress)
            return addr.getHostAddress
        }
      }
      ipaddress
    }
  }

  lazy val getLocalHostName: VitalsHostName = InetAddress.getLocalHost.getHostName
  lazy val getPublicHostName: VitalsHostName = convertLocalHostnameToExternal(getLocalHostName)

  lazy val getLocalHostAddress: VitalsHostAddress = InetAddress.getLocalHost.getHostAddress
  lazy val getPublicHostAddress: VitalsHostAddress = convertLocalAddressToExternal(getLocalHostAddress)

  def convertHostnameToAddress(hostname: VitalsHostName): VitalsHostAddress = {
    try {
      InetAddress.getByName(hostname) match {
        case null =>
          val msg = burstStdMsg(s"hostname: '$hostname' not found")
          log error msg
          throw new RuntimeException(msg)
        case address => address.getHostAddress
      }
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(s"hostname: '$hostname'", t)
        log error(msg, t)
        throw new RuntimeException(msg, t)
    }

  }

  def convertHostAddressToHostname(ipAddress: VitalsHostAddress): VitalsHostName = {
    try {
      InetAddress.getByName(ipAddress) match {
        case null =>
          val msg = burstStdMsg(s"ipAddress: '$ipAddress' not found")
          log error msg
          throw new RuntimeException(msg)
        case address => address.getHostName // TODO this does not always work...
      }
    } catch safely {
      case t: Throwable =>
        val msg = burstStdMsg(s"hostname: '$ipAddress'", t)
        log error(msg, t)
        throw new RuntimeException(msg, t)
    }

  }

}
