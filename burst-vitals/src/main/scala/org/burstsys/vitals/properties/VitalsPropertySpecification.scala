/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.properties

import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.properties.VitalsPropertyRegistry.sourcePadding
import org.burstsys.vitals.strings._

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * centralized Burst management of configuration properties
 *
 * @param key
 * @param description
 * @param default
 * @tparam C
 */
final case
class VitalsPropertySpecification[C <: VitalsPropertyAtomicDataType : ClassTag](
                                                                                 key: String, description: String,
                                                                                 sensitive: Boolean = false,
                                                                                 default: Option[C] = None
                                                                               ) {

  VitalsPropertyRegistry += this

  val typeName: String = classTag[C].runtimeClass.getSimpleName.initialCase

  val environmentVariableKey: String = propertyToEnvironment(key)

  lazy val listeners: mutable.Set[Option[C] => Unit] = ConcurrentHashMap.newKeySet[Option[C] => Unit].asScala

  private var setProgrammatically = false

  def useDefault(): Unit = {
    System.clearProperty(key)
  }

  def set(value: C): Unit = {
    if (value != null) {
      System.setProperty(key, value.toString)
      setProgrammatically = true
    } else {
      useDefault()
      setProgrammatically = false
    }

    val current = get
    listeners.foreach(l => l(current))
  }

  /**
   * @return the value of this property
   * @throws VitalsException if the property is not set and has a default of `None`
   */
  def getOrThrow: C = {
    get match {
      case None => throw VitalsException(s"label=$key not found and no default provided")
      case Some(value) => value
    }
  }

  def get: Option[C] = {
    System.getenv(environmentVariableKey) match {
      case null => System.getProperty(key) match {
        case null => default
        case _ => Some(property(key, fallback))
      }
      case _ => Some(environment(environmentVariableKey, fallback))
    }
  }

  def source: String = {
    if (setProgrammatically)
      "runtime"
    else if (System.getenv(environmentVariableKey) != null)
      "env var"
    else if (System.getProperty(key) != null)
      "java prop"
    else
      "default"
  }

  def fallback: C = {
    default match {
      case Some(value) => value.asInstanceOf[C]
      case None =>
        val e = classTag[C].runtimeClass
        val value = if (e == classOf[Boolean]) {
          false
        } else if (e == classOf[Long]) {
          0L
        } else if (e == classOf[Int]) {
          0
        } else if (e == classOf[Double]) {
          0.0
        } else if (e == classOf[String]) {
          ""
        } else {
          throw VitalsException(s"unsupported default type '$e'")
        }
        value.asInstanceOf[C]
    }
  }

  val keyPadding = 41
  val envVarPadding = 41
  val typeNamePadding = 10
  val descriptionPadding = 40

  override
  def toString: String = {
    val typeStr = s"[$typeName]".padded(typeNamePadding)
    val keyName = s"$key".padded(keyPadding)
    val sourceStr = s"[$source]".padded(sourcePadding)
    val desc = s"${description.initialCase}".padded(descriptionPadding)
    s" $typeStr $keyName $sourceStr - $desc [ ${get.map(v => if (sensitive) "REDACTED" else v).getOrElse("None")} ]"
  }

  private def propertyToEnvironment(key: String): String = {
    key.replace('.', '_').toUpperCase
  }

}

