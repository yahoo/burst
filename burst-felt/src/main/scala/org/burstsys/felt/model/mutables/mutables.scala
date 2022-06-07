/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.mutables.valarr.FeltMutableValArrProv
import org.burstsys.felt.model.mutables.valmap.FeltMutableValMapProv
import org.burstsys.felt.model.mutables.valset.FeltMutableValSetProv

/**
 * =mutables=
 *
 */
package object mutables extends FeltMutableSymbols {

  /**
   * ==Felt Mutables==
   * <hr/>
   * A mutable is a ''temporary within a scan lifecycle'', high performance variable size (collection) FELT data structure.
   * This universal (pure) trait is implemented by another package to assist the FELT model in providing semantics
   * that require runtime data structures that are temporary collections within a scan.
   * <br/><br/>
   * ===Semantics===
   * <ol>
   * <li>'''ALLOC OPS:''' Each mutable has to be allocated and de-allocated at some appropriate points in the scan. These operations
   * are the same for all mutables.</li>
   * <li>'''READ/WRITE OPS:''' Each mutable has operations that write and read the contents.  These operations vary based on the semantics
   * of the particular mutable involved</li>
   * </ol>
   */
  trait FeltMutable extends Any {
    def clear(): Unit
  }


  /**
   * the complete set of Felt mutable providers (value arrays, sets, maps)
   */
  trait FeltMutableProviders extends Any {

    /**
     * provider for mutable value arrays
     *
     * @return
     */
    def valarr: FeltMutableValArrProv

    /**
     * provider for mutable value sets
     *
     * @return
     */
    def valset: FeltMutableValSetProv

    /**
     * provider for mutable value maps
     *
     * @return
     */
    def valmap: FeltMutableValMapProv

  }

}
