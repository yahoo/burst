/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.execution.model.execute

import org.burstsys.vitals.logging._

package object parameters extends VitalsLogger {

  sealed case class FabricParameterForm(code: Int) {
    override def toString: String = getClass.getSimpleName.stripPrefix("Fabric").stripSuffix("Form$")
  }

  object FabricScalarForm extends FabricParameterForm(1)

  object FabricVectorForm extends FabricParameterForm(2)

  object FabricMapForm extends FabricParameterForm(3)


}
