/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio

import org.burstsys.brio.dictionary.flex.BrioDictionaryBuilder
import org.burstsys.vitals.reporter.VitalsByteQuantReporter

package object dictionary {

  final val partName: String = "dictionary"

  final val defaultDictionaryBuilder = BrioDictionaryBuilder()

  object BrioDictionaryReporter extends VitalsByteQuantReporter("brio", "dictionary")

}
