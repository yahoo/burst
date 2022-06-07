/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql.generators.hydra.frames

import org.burstsys.eql.actions.{ControlExpression, TemporaryExpression}

trait Controlled {
  def addPlacedControl(controls: Array[PlacedControl]): this.type

}

case class PlacedControl(control: ControlExpression, temporaries: Array[TemporaryExpression])
