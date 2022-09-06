/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import org.burstsys.eql.context.EqlContextImpl
import org.burstsys.eql.generators.Declaration
import org.burstsys.eql.paths.DynamicVisitPath
import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters._

package object eql {
  object EqlContext {
    def apply(guid: FabricGroupUid): EqlContext = new EqlContextImpl(guid)
  }

  val FramePropertyName = "frameProperty"
  val AnalysisPropertyName = "analysisProperty"
  def qualifiedName(name: String)(implicit context:GlobalContext): String = s"$qualifiedFrameName.$name"
  def qualifiedFrameName(implicit context:GlobalContext): String = s"${context(AnalysisPropertyName)}.${context(FramePropertyName)}"

  class GlobalContext() {
    private val temporaryCounter = new AtomicInteger(1)

    private val props = new ConcurrentHashMap[String, String]()

    private val declarations = new ConcurrentHashMap[String, Declaration]()

    private val attachmentPoints = new ConcurrentHashMap[DynamicVisitPath, String]()

    def reset: this.type = {
      temporaryCounter.set(1)
      now = System.currentTimeMillis()
      attachmentPoints.clear()
      this
    }

    def temporaryName: String = s"T${temporaryCounter.getAndIncrement()}"

    var now: Long = System.currentTimeMillis()

    def addProperty(key: String, value: String): String = props.put(key, value)

    def getProperty(key: String): String = props.get(key)

    def removeProperty(key: String): String = props.remove(key)

    def  apply(key: String): String = getProperty(key)

    def addDeclaration(key: String, value: Declaration): Declaration = declarations.put(key, value)

    def getDeclaration(key: String): Declaration = declarations.getOrDefault(key, null)

    def removeDeclaration(key: String): Declaration = declarations.remove(key)

    def addIfAbsentAttachment(dvp: DynamicVisitPath): String = {
      val name = s"ext_${attachmentPoints.size()}"
      val res = attachmentPoints.putIfAbsent(dvp, name)
      if (res != null)
        res
      else
        name
    }

    def getAttachment(dvp: DynamicVisitPath): String = attachmentPoints.getOrDefault(dvp, null)

    def getAttachments: Map[DynamicVisitPath, String] = attachmentPoints.asScala.toMap
  }

  trait EqlContext {
    def eqlToHydra(schemaName: Option[String], source: String):  String

    def globalContext: GlobalContext
  }
}
