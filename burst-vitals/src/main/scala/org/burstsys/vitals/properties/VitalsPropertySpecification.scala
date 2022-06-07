/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.properties

import java.util.concurrent.ConcurrentHashMap

import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.strings._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

/**
  * centralized Burst management of configuration properties
  *
  * @param key
  * @param description
  * @param exportToWorker
  * @param default
  * @tparam C
  */
final case
class VitalsPropertySpecification[C <: VitalsPropertyAtomicDataType : ClassTag](
                                                                                 key: String, description: String,
                                                                                 hidden: Boolean = false,
                                                                                 exportToWorker: Boolean = true,
                                                                                 default: Option[C] = None
                                                                               ) {

  VitalsPropertyRegistry += this

  val typeName: String = classTag[C].runtimeClass.getSimpleName.initialCase

  val environmentVariableKey: String = propertyToEnvironment(key)

  lazy val listeners: mutable.Set[Option[C] => Unit] = ConcurrentHashMap.newKeySet[Option[C] => Unit].asScala

  def useDefault(): Unit = {
    System.clearProperty(key)
  }

  def set(value: C): Unit = {
    if (value != null)
      System.setProperty(key, value.toString)
    else useDefault()

    val current = get
    listeners.foreach(l => l(current))
  }

  /**
    * try for this in the process environment, then the java properties, then either an optional default
    * or an exception...
    *
    * @return
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
    val keyName = s"$key".padded(keyPadding)
    val envVar = environmentVariableKey.padded(envVarPadding)
    val exported = if (exportToWorker) "*" else " "
    val typeNameStr = s"[$typeName]".padded(typeNamePadding)
    val desc = s"${description.initialCase}".padded(descriptionPadding)
    s"$exported$typeNameStr$keyName$envVar- $desc [ ${get.getOrElse("None")} ]"
  }

  private def propertyToEnvironment(key: String): String = {
    key.replace('.', '_').toUpperCase
  }

}

