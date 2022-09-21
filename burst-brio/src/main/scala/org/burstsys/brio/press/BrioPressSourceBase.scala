/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.press

import org.burstsys.vitals.errors.VitalsException

/**
  * A base class for press sources to use, provides convenience methods to press
  */
abstract class BrioPressSourceBase extends BrioPressSource with BrioValuePresser {

  def unknownParentInstance(parent: BrioPressInstance, relationName: String): Nothing = {
    val message = s"Encountered unknown press instance during pressing ${parent.getClass.getName}.$relationName"
    log warn message
    throw VitalsException(message)
  }

}
