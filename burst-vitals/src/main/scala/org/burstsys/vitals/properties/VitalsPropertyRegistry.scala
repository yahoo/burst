/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.properties

import org.burstsys.vitals.logging._
import org.burstsys.vitals.reflection
import org.burstsys.vitals.strings._

import scala.collection.mutable

trait VitalsPropertyRegistry

object VitalsPropertyRegistry extends VitalsLogger {
  val keyPadding = 50
  val sourcePadding = 11
  val typeNamePadding = 10
  val descriptionPadding = 40

  val hdr: String = s" ${"TYPE".padded(typeNamePadding)} ${"KEY".padded(keyPadding)} ${"SOURCE".padded(sourcePadding)}   ${"DESCRIPTION".padded(descriptionPadding)} VALUE"
  val sep: String = "-" * (hdr.length + 5)

  private[this]
  val _registry = new mutable.HashMap[String, VitalsPropertySpecification[_]]

  private[properties]
  lazy val registry: Map[String, VitalsPropertySpecification[_]] = {
    reflection.getPackageSubTypesOf(classOf[VitalsPropertyRegistry])
    _registry.toMap
  }

  def +=(property: VitalsPropertySpecification[_]): Unit = {
    _registry += property.key -> property
  }

  lazy val logReport: this.type = {
    log info printAllProperties
    this
  }

  private def printAllProperties: String = {
    val properties = registry.keys.toList.sorted
      .map(property => {
        s"${registry(property)} \n"
      })
      .mkString("").trimAtEnd
    s"""
       |$sep
       |$hdr
       |$properties
       |$sep
       |""".stripMargin
  }

  def importProperties(properties: Map[String, String]): this.type = {
    properties.foreach {
      case (k, v) => System.setProperty(k, v)
    }
    this
  }

  def allProperties: Map[String, VitalsPropertySpecification[_]] = {
    _registry.filterNot(kv => kv._2.sensitive).toMap
  }
}
