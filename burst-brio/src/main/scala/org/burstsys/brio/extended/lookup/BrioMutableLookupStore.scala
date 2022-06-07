/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.extended.lookup

/**
  * This is used during pressing of brio blobs to collect information for
  * lookups that are in turn pressed into [[BrioStaticLookupTable]] form
  */
final case
class BrioMutableLookupStore() extends AnyRef {

  /** *
    *
    * @return
    */
  def lookupTable(): BrioMutableLookupTable = ???

}
