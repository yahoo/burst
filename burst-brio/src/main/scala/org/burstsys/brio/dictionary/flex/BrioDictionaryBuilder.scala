/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary.flex

import org.burstsys.brio.dictionary.flex
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder

/**
 * parameters to build dictionaries with some defaults...
 */
final case class BrioDictionaryBuilder() extends TeslaPartBuilder {
  override def defaultStartSize: TeslaMemorySize = flex.flexDictionaryDefaultStartSize
}
