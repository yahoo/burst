/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.json

import org.burstsys.samplesource.SampleSourceVersion

package object samplestore {

  final case class JsonStoreCmdLineArguments(
                                               standAlone: Boolean = false
                                             )

  final val jsonStoreCmdLineArguments = JsonStoreCmdLineArguments()
  final val JsonSampleStoreName = "json-sample"
  final val JsonBrioSampleSourceName = "JsonBrio"
  final val JsonBrioSampleSourceVersion: SampleSourceVersion = "1.0"

}
