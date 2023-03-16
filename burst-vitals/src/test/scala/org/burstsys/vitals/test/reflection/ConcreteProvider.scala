/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */

package org.burstsys.vitals.test.reflection

case class ConcreteProvider() extends TestProvider {
  override def test: Boolean = true
}
