/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */

package test.burstsys

import org.burstsys.vitals.test.reflection.TestProvider

case class OutsideConcreteProvider() extends TestProvider {
  override def test: Boolean = true
}
