/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio

import org.burstsys.brio.model.schema.registerBrioSchema
import org.burstsys.brio.press.BrioPressSource
import org.burstsys.vitals.errors._
import org.burstsys.vitals.logging.{VitalsLogger, _}
import org.burstsys.vitals.reflection

import scala.collection.JavaConverters._

package object provider extends VitalsLogger {

  /**
   * Classes that implement this interface will be found at runtime and registered with the brio subsystem.
   */
  trait BrioSchemaProvider[P <: BrioPressSource] extends Any {

    /**
     * the name(s) for this provider. All names are equivalent aliases for each other.
     * these are all case insensitive
     *
     * @return
     */
    def names: Array[String]

    final
    def printNames: String = names.mkString("('", "', '", "')")

    /**
     * The the class path leading to brio schema file as a resource
     *
     * @return
     */
    def schemaResourcePath: String

    /**
     * The brio presser class
     *
     * @return
     */
    def presserClass: Class[P]

    override final def toString: String = {
      val presserClassName = if(presserClass == null) "no-presser" else presserClass.getSimpleName
      s"BrioSchemaProvider(names=$printNames}, schemaResourcePath='$schemaResourcePath', presserClass=$presserClassName)"
    }

  }

  private
  lazy val schemaProviders: Array[BrioSchemaProvider[_]] = {
    val scannedClasses = reflection.getSubTypesOf(classOf[BrioSchemaProvider[_]])
    scannedClasses.asScala.map(_.newInstance()).toArray
  }

  private[this]
  var _loaded = false

  /**
   * load the schema providers from the class path
   */
  final
  def loadBrioSchemaProviders(): Unit = {
    synchronized {
      if (_loaded) return
      schemaProviders.foreach {
        provider =>
          try {
            log info s"BRIO_SCHEMA_PROVIDER_LOAD: $provider"
            registerBrioSchema(this.getClass, provider.schemaResourcePath, provider.names: _*)
          } catch safely {
            case t: Throwable =>
              log error burstStdMsg(s"BRIO_SCHEMA_PROVIDER_LOAD_FAIL provider='$provider' $t", t)
          }
      }
      _loaded = true
    }
  }

}
