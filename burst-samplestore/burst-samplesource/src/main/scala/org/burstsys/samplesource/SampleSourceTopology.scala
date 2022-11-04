/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplesource

import org.burstsys.samplestore.api.SampleStoreDataLocus

trait SampleStoreTopology {
  def loci: Iterable[SampleStoreDataLocus]
}

object SampleStoreTopology {
  def apply(loci: Iterable[SampleStoreDataLocus]): SampleStoreTopology =
    new SampleStoreTopologyContext(loci)
}

private class SampleStoreTopologyContext(override val loci: Iterable[SampleStoreDataLocus]) extends SampleStoreTopology

