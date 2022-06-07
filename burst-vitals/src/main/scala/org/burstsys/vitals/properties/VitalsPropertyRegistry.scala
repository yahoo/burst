/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.properties

import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.logging._
import org.burstsys.vitals.reflection
import org.burstsys.vitals.strings._

import scala.collection.JavaConverters._
import scala.collection.mutable

trait VitalsPropertyRegistry

object VitalsPropertyRegistry extends VitalsLogger {
  val hdr: String = " TYPE      KEY                                      ENV_VAR                                  DESCRIPTION                                VALUE"
  val sep: String = "---------------------------------------------------------------------------------------------------------------------------------------------------"

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
      .collect({ case k if !registry(k).hidden => registry(k) })
      .map { property =>
        property.get   // force the load
        s"$property \n"
      }
      .stringify.trimAtEnd
    s"""
       |$sep
       |$hdr
       |$properties
       |$sep
       |""".stripMargin
  }

  def exportToWorkerAsStrings: Array[String] = {
    registry.filter(_._2.exportToWorker).map {
      case (k, v) =>
        s"""-D$k="${v.get.getOrElse("")}""""
    }.toArray
  }

  def importProperties(properties: Map[String, String]): this.type = {
    properties.foreach {
      case (k, v) => System.setProperty(k, v)
    }
    this
  }

  def allProperties: Map[String, VitalsPropertySpecification[_]] = {
    _registry.filterNot(kv => kv._2.hidden).toMap
  }
}
