/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.runtime

import org.burstsys.brio.blob.BrioBlob
import org.burstsys.brio.dictionary.BrioDictionary
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.tesla.buffer.TeslaBufferReader

/**
 * State associated with the data for a single Brio Blob/Fabric Item
 * NOTE: this is all read-only data.
 */
trait BrioItemRuntime extends AnyRef {

  /////////////////////////////////////
  // State
  /////////////////////////////////////

  @transient private[this]
  var _item: BrioBlob = _

  @transient private[this]
  var _reader: TeslaBufferReader = _

  @transient private[this]
  var _dictionary: BrioDictionary = _

  /////////////////////////////////////
  // API
  /////////////////////////////////////

  @inline final
  def prepareBrioItemRuntime(): Unit = {
    _item = null
    _reader = null
    _dictionary = null
  }

  /**
   * we set the readers and dictionary variables here because the static blobs have to look
   * it up each time which requires CPU cycles. We don't want to call the reader/dictionary
   * methods on item often.
   *
   * @param item
   */
  @inline final
  def prepare(item: BrioBlob): BrioItemRuntime = {
    _item = item
    if (item != null) {
      _reader = item.data // time consuming for static blobs
      _dictionary = item.dictionary // time consuming for static blobs
    } else {
      _reader = null
      _dictionary = null
    }
    this
  }

  @inline final
  def currentItem: BrioBlob = {
    _item
  }

  /**
   * the reader for the blob data (read only)
   *
   * @return
   */
  @inline final
  def reader: TeslaBufferReader = {
    _reader
  }

  /**
   * the dictionary for the brio lattice (read-only)
   *
   * @return
   */
  @inline final
  def dictionary: BrioDictionary = {
    _dictionary
  }

}
