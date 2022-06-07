/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.test

import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.vitals.logging._

package object press extends VitalsLogger {

  trait PresserInstance extends BrioPressInstance {
    final override val schemaVersion: BrioVersionKey = 2
  }

  case
  class RootStructure(f0: String, f1: Long, f2: Short, f3: SecondLevelStructure, f4: Array[SecondLevelStructure],
                      added: AddedStructure, application: ApplicationStructure) extends PresserInstance

  final case
  class SecondLevelStructure(f0: Long, f1: Long, f2: Double, f3: Array[ThirdLevelStructure], f4: ThirdLevelStructure)
    extends PresserInstance

  final case
  class ThirdLevelStructure(f0: Long, f1: Long, f2: Map[String, String], f3: Array[Double]) extends PresserInstance

  final case
  class AddedStructure(f0: String, f1: Double, f2: Array[String], f3: Boolean, f4: Boolean) extends PresserInstance

  // For testing the Unity schema, containing multiple appearances of Use.

  final case class ApplicationStructure(firstUse: UseStructure, mostUse: UseStructure, lastUse: UseStructure) extends PresserInstance

  final case class UseStructure(tag: String) extends PresserInstance

}
