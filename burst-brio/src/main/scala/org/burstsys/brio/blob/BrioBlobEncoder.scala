/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.blob

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioTypes.BrioVersionKey
import org.burstsys.tesla
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.BlobEncodingVersion2
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer

object BrioBlobEncoder {
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
    * Take a pressed brio object model, a version, the dictionary, and place an encoded blob
    * into the provided buffer. This is  V2 (current recommended) encoding format. There are other
    * legacy ones that are marked deprecated that we are trying very hard to get rid of.
    *
    * @param buffer
    * @param rootVersion
    * @param dictionary
    * @param v2Blob
    */
  final
  def encodeV2Blob(buffer: TeslaMutableBuffer, rootVersion: BrioVersionKey,
                   dictionary: BrioMutableDictionary, v2Blob: TeslaMutableBuffer): Unit = {

    //////////////////////////////////////////////////////////////
    // BLOB ENCODING START
    //////////////////////////////////////////////////////////////

    // set up appropriate memory pointers and offset cursors.
    var v2BlobCursor = 0

    //////////////////////////////////////////////////////////////
    // BLOB FIELD 1:  ENCODING FORMAT VERSION
    //////////////////////////////////////////////////////////////
    v2Blob.writeInt(BlobEncodingVersion2, v2BlobCursor)
    v2BlobCursor += SizeOfInteger

    //////////////////////////////////////////////////////////////
    // BLOB FIELD 2:  ROOT OBJECT VERSION
    //////////////////////////////////////////////////////////////
    v2Blob.writeInt(rootVersion, v2BlobCursor)
    v2BlobCursor += SizeOfInteger

    //////////////////////////////////////////////////////////////
    //  BLOB FIELD 3: DICTIONARY SIZE
    //////////////////////////////////////////////////////////////
    val dictionarySize = dictionary.currentMemorySize
    v2Blob.writeInt(dictionarySize, v2BlobCursor)
    v2BlobCursor += SizeOfInteger

    //////////////////////////////////////////////////////////////
    //  BLOB FIELD 4: DICTIONARY DATA
    //////////////////////////////////////////////////////////////
    {
      var i = 0
      while (i < dictionary.currentMemorySize) {
        tesla.offheap.putByte(
          v2Blob.dataPtr + v2BlobCursor + i,
          tesla.offheap.getByte(dictionary.basePtr + i)
        )
        i += 1
      }
    }
    v2BlobCursor += dictionary.currentMemorySize

    //////////////////////////////////////////////////////////////
    //  BLOB FIELD 5: ROOT OBJECT SIZE
    //////////////////////////////////////////////////////////////
    val rootObjectBufferSize = buffer.currentUsedMemory
    v2Blob.writeInt(rootObjectBufferSize, v2BlobCursor)
    v2BlobCursor += SizeOfInteger

    //////////////////////////////////////////////////////////////
    // BLOB FIELD 6: ROOT OBJECT DATA
    //////////////////////////////////////////////////////////////
    {
      var i = 0
      while (i < rootObjectBufferSize) {
        tesla.offheap.putByte(
          v2Blob.dataPtr + v2BlobCursor + i,
          tesla.offheap.getByte(buffer.dataPtr + i)
        )
        i += 1
      }
    }

    v2BlobCursor += rootObjectBufferSize // add size of root object data

    v2Blob.currentUsedMemory(v2BlobCursor)
  }

}
