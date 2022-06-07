/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model

package object mode {

  /**
   * fabric data load modality
   *
   * @param name
   */
  sealed case class FabricLoadMode(name: String) {
    override def toString: String = getClass.getSimpleName.stripPrefix("Fabric").stripSuffix("$")
  }

  object FabricUnknownLoad extends FabricLoadMode("unknown")

  object FabricColdLoad extends FabricLoadMode("cold")

  object FabricWarmLoad extends FabricLoadMode("warm")

  object FabricHotLoad extends FabricLoadMode("hot")

  object FabricNoDataLoad extends FabricLoadMode("NO-DATA")

  object FabricErrorLoad extends FabricLoadMode("error")


}
