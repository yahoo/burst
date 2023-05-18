/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test

import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Suite
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class FabricWaveBaseSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach
  with FabricSpecLog {
}
