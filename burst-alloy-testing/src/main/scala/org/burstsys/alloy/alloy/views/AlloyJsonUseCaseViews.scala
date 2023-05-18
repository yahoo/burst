/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.views

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.burstsys.alloy.alloy.store.AlloyView
import org.burstsys.alloy.alloy.{getResourceFile, makeJsonFile}
import org.burstsys.alloy.store.mini.MiniView
import org.burstsys.alloy.views.quo.QuoUseCaseViews
import org.burstsys.alloy.views.unity.UnityUseCaseViews
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.fabric.wave.metadata.model
import org.burstsys.vitals.time.VitalsTimeZones
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, DateTimeZone}

import scala.language.implicitConversions

object AlloyJsonUseCaseViews {

  val f: DateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy").withZone(DateTimeZone.forID(VitalsTimeZones.VitalsDefaultTimeZoneName))
  val nowTime: DateTime = f.parseDateTime("11/02/2018")

  private implicit def dateTimeToMillis(t: DateTime): Long = t.getMillis

  val unitySchema: BrioSchema = BrioSchema("unity")
  val quoSchema: BrioSchema = BrioSchema("quo")

  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def miniToAlloy(miniViews:  Array[MiniView]): Array[AlloyView] = {
    miniViews.map{mv =>
      val iJsons = mv.items.map(mapper.writeValueAsString(_))
      val name = makeJsonFile(s"${mv.schema.name}-${mv.domainKey}-${mv.viewKey}", iJsons)
      AlloyJsonFileView(mv.schema, mv.domainKey, mv.viewKey, name)
    }
  }

  def generateAlloy(schema: BrioSchema, domainKey: model.FabricDomainKey,  viewKey: model.FabricViewKey, generator:  Iterator[BrioPressInstance]): AlloyView = {
    val name = makeJsonFile(s"${schema.name}-${domainKey}-${viewKey}", generator)
    AlloyJsonFileView(schema, domainKey, viewKey, name)
  }

  val views: Array[AlloyView] = miniToAlloy(UnityUseCaseViews.views)
  val quoViews: Array[AlloyView] = miniToAlloy(QuoUseCaseViews.views)
  val quoSpecialView: AlloyView = AlloyJsonFileView(quoSchema, 1, 1, getResourceFile("quo/quo-view.json.gz"))
}
