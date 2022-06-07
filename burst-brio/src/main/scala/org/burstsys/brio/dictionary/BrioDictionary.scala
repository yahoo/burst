/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.dictionary

import org.burstsys.brio.dictionary.mutable.BrioMutableDictionary
import org.burstsys.brio.types.BrioTypes.{BrioDictionaryKey, BrioDictionaryNotFound}
import org.burstsys.tesla.block.TeslaBlockPart
import org.burstsys.vitals.text.VitalsTextCodec

/**
  * =Brio  Dictionary=
  * This is a bidirectional UTF8 `string->key` and `key->string` mapping combined into a single
  * zero object allocation data structure.
  * It is implemented using Scala value class wrapping a single off heap memory block so there are
  * zero JVM objects used anywhere in the creation and usage of this data structure.
  *
  * ===Design Rules:===
  * {{{
  *   1) The number of buckets is fixed in order to simplify the overall design.
  *   2) The dictionary can have words added, but it cannot have word->key or key->word mappings changed
  *   3) no java objects are created during creation and access
  *   4) This is a init time fixed size off heap block of memory - no increases in max size supported as of now
  *   5) an error is thrown if you put more words in than there is storage for in the fixed size
  *   6) it is desirable that these are allocated in grids along with other dictionaries in
  *      much larger malloc calls to reduce malloc overhead
  *   7) It is possible that per-thread grid allocations are optimal to keep cores in disparate memory regions
  *   8) these rely on the global direct memory block allocation library
  *   9) In its mutable form, this is an append only data structure - we can update fields in header and buckets, but we cannot
  *      add anything except at the end.
  * }}}
  * = BRIO DICTIONARY INTERNALS =
  * A useful central location of the internal binary structure of the off heap dictionary
  * == Dictionary ==
  * the top level structure with a fixed size header followed by variable size slot storage
  * {{{
  * | WORDS             | INTEGER                         | count of words in dictionary (-1 for overflow)
  * | POOL ID           | INTEGER                         | local pool identifier
  * | NEXT SLOT OFFSET  | INTEGER                         | variable offset into memory block for additional slot storage (this always points to end of dictionary)
  * | BUCKETS           | ARRAY[INTEGER]                  | fixed location, fixed size array of buckets values - each value is offset to first slot in associated bucket list
  * | SLOTS             | ARRAY[BrioDictionarySlotState]  | variable size array of slots each of which is a  BrioDictionarySlotState
  * }}}
  *
  * == SLOTS ==
  * one of these for each string stored in the dictionary stored at the end of the dictionary and
  * growing until the dictionary runs out of slots (''overflow'')
  * {{{
  * | SIZE  |  SHORT        | size in bytes of contained string
  * | NEXT  |  INTEGER      | offset of next string slot
  * | DATA  |  ARRAY[BYTE]  | the string bytes in UTF8
  * }}}
  *
  */
trait BrioDictionary extends Any   {

  /**
    * returns a dictionary key for a string in the dictionary, [[BrioDictionaryNotFound]] if not found or if
    * dictionary has overflowed
    *
    * @param string
    * @return
    */
  def keyLookup(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey

  /**
    * returns a string for a key, null if not found, or if the dictionary has overflowed
    *
    * @param key
    * @return
    */
  def stringLookup(key: BrioDictionaryKey)(implicit text: VitalsTextCodec): String

  /**
    * used for debugging mostly
    *
    * @param text
    * @return
    */
  def dump(implicit text: VitalsTextCodec): String

  /**
    * return all the keys in the dictionary
    *
    * @param text
    * @return
    */
  def keySet(implicit text: VitalsTextCodec): Array[BrioDictionaryKey]

  /**
    * how many words in the dictionary
    *
    * @return
    */
  def words: Int

  /**
    * either key or slot exhaustion
    *
    * @return
    */
  def overflowed: Boolean

  /**
    * key space exhaustion
    *
    * @return
    */
  def keyOverflowed: Boolean

  /**
    * slot space exhaustion
    *
    * @return
    */
  def slotOverflowed: Boolean

  /**
    * lookup string and return key -- create if not already there. Return [[BrioDictionaryNotFound]] if
    * dictionary has overflowed. Throws exception if null string is passed
    *
    * @param string
    * @return
    */
  def keyLookupWithAdd(string: String)(implicit text: VitalsTextCodec): BrioDictionaryKey


}
