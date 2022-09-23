/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore

import org.burstsys.vitals.logging._
import org.burstsys.vitals.net.{VitalsHostAddress, VitalsHostPort}
import org.burstsys.vitals.properties._
import org.burstsys.vitals.uid._

import scala.language.implicitConversions

package object api extends VitalsLogger {

  final val SampleStoreSourceNameProperty = "burst.samplestore.source.name" // String, required
  final val SampleStoreSourceVersionProperty = "burst.samplestore.source.version" // String, required

  //////////////////////////////////////////////////////////
  // DataLocus
  //////////////////////////////////////////////////////////

  type SampleStoreDataLocus = BurstSampleStoreApiDataLocus.Proxy

  object SampleStoreDataLocus {
    def apply(locus: SampleStoreDataLocus): SampleStoreDataLocus =
      BurstSampleStoreApiDataLocus(locus.suid, locus.hostAddress, locus.hostName, locus.port, locus.partitionProperties)
    def apply(
               suid: VitalsUid,
               ipAddress: String,
               hostName: VitalsHostAddress,
               port: VitalsHostPort,
               partitionProperties: VitalsPropertyMap
             ): SampleStoreDataLocus = {
      BurstSampleStoreApiDataLocus(suid, ipAddress, hostName, port, partitionProperties)
    }
  }

  final case class SampleStoreDataLocusContext(_underlying_BurstSampleStoreApiDataLocus: BurstSampleStoreApiDataLocus)
    extends BurstSampleStoreApiDataLocus.Proxy

  implicit def DataLocusApiToProxy(a: BurstSampleStoreApiDataLocus): SampleStoreDataLocus =
    SampleStoreDataLocusContext(a)

  implicit def DataLocusProxyToApi(a: SampleStoreDataLocus): BurstSampleStoreApiDataLocus =
    BurstSampleStoreApiDataLocus(a.suid, a.hostAddress, a.hostName, a.port, a.partitionProperties)


  //////////////////////////////////////////////////////////
  // Generator
  //////////////////////////////////////////////////////////

  final case
  class SampleStoreGeneration(
                              guid: VitalsUid,
                              generationHash: String, // a quick way to compare one loci set to another different one
                              loci: Array[SampleStoreDataLocus],
                              schema: String,
                              motifFilter: BurstMotifFilter = None
                            )

  //////////////////////////////////////////////////////////
  // Exceptions - these map directly to the Request States in BurstSampleStoreApiRequestState
  // The default one maps to BurstSampleStoreApiRequestException
  //////////////////////////////////////////////////////////
  final case
  class SampleStoreApiRequestTimeoutException(msg: String, t: Throwable) extends RuntimeException(msg, t)

  final case
  class SampleStoreApiRequestInvalidException(msg: String, t: Throwable) extends RuntimeException(msg, t)

  final case
  class SampleStoreApiNotReadyException(msg: String, t: Throwable) extends RuntimeException(msg, t)

}
