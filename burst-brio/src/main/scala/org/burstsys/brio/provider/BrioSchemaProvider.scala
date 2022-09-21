/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.provider

/**
 * Classes that implement this interface will be found at runtime and registered with the brio subsystem.
 */
trait BrioSchemaProvider extends Any {

  /**
   * All names are equivalent aliases for each other, these are all case insensitive
   * @return the name(s) for this provider
   */
  def names: Array[String]

  /**
   * @return The the class path leading to brio schema file as a resource
   */
  def schemaResourcePath: String

  override final def toString: String = {
    s"BrioSchemaProvider(names=${names.mkString("('", "', '", "')")}, schemaResourcePath='$schemaResourcePath')"
  }

}
