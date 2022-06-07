/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.sweep.lexicon

import org.burstsys.brio.types.BrioTypes
import org.burstsys.felt.model.tree.code.{FeltCodeCursor, _}
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.strings._

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.JavaConverters._

/**
 * a manager for runtime string->key mappings as used by the Felt system to
 * do lookup of string values/nullity from dictionary at runtime prepare
 * time and save all the UTF8 codec overhead
 * extract strings from the AST in order to convert to keys in current dictionary. Avoid UTF8 codec
 * stupidity and needless dictionary lookups. Lookup once for each new blob and store in the runtime.
 * There are two locations for strings - immutable strings from the Brio Blob's static brio dictionary and
 * strings that are 'inserted' into mutable collectors such as cubes. Someday we may need to worry about the
 * latter but for now we don't because the mutable collectors are so far not readable so cannot be used
 * in a FELT expression.
 *
 * @return
 */
trait FeltLexicon extends Any {

  /**
   * is the lexicon enabled?
   * We turn it off for some expressions that at this point we cannot resolve using it
   *
   * @return
   */
  def enabled: Boolean

  /**
   * turn off the lexicon feature.
   * We turn it off for some expressions that at this point we cannot resolve using it.
   * NOTE: once disabled, the lexicon cannot be enabled. This is to prevent a rule from
   * turning it on when another rule has decided its unworkable.
   */
  def disable(): Unit

  /**
   * lookup an index for a string (not the dictionary key!)
   *
   * @param str
   * @return
   */
  def lookupIndex(str: String): Int

  /**
   * variable name for value
   *
   * @param index
   * @return
   */
  def valueName(index: Int): String

  /**
   * variable name for nullity
   *
   * @param index
   * @return
   */
  def nullityName(index: Int): String

  /**
   * generate prepare code to do brio blob static dictionary lookups
   *
   * @param cursor
   * @return
   */
  def genRtLexPrepareBlk(implicit cursor: FeltCodeCursor): FeltCode

  /**
   * generate code to declare all strings in the lexicon
   *
   * @param cursor
   * @return
   */
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode

}

object FeltLexicon {
  def apply(): FeltLexicon = FeltLexiconContext()
}

private case
class FeltLexiconContext() extends FeltLexicon {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  var _enabled = !globalLexiconDisable

  private[this]
  val _stringIndex = new AtomicInteger

  private[this]
  val _stringToIndexMap = new ConcurrentHashMap[String, Int].asScala

  private[this]
  val _indexToStringMap = new ConcurrentHashMap[Int, String].asScala

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override def enabled: Boolean = _enabled

  final override def disable(): Unit = _enabled = false

  final override
  def lookupIndex(str: String): Int = {
    _stringToIndexMap.get(str) match {
      case None =>
        val index = _stringIndex.incrementAndGet
        _stringToIndexMap += str -> index
        _indexToStringMap += index -> str
        index
      case Some(s) => s
    }
  }

  final override
  def genRtLexPrepareBlk(implicit cursor: FeltCodeCursor): FeltCode = {
    if (!_enabled) return FeltNoCode
    val code = _stringToIndexMap.map {
      case (str, index) =>
        s"""|
            |$I{ val key = dictionary.keyLookup("$str"); if(key == ${BrioTypes.BrioDictionaryNotFound}) { ${nullityName(index)} = true; } else { ${nullityName(index)} = false; ${valueName(index)} = key; } }""".stripMargin
    }.stringify
    s"""|
        |${C("lexicon initializers - do before parameter/global variable initializations")}$code""".stripMargin
  }

  final override
  def generateDeclaration(implicit cursor: FeltCodeCursor): FeltCode = {
    if (!_enabled) return FeltNoCode
    _stringToIndexMap.map {
      case (str, index) =>
        s"""|
            |${I}var ${valueName(index)}:Short = ${BrioTypes.BrioDictionaryNotFound}; var ${nullityName(index)}:Boolean = true; // '$str'""".stripMargin
    }.stringify
  }

  final override
  def valueName(index: Int): String = {
    if (!_enabled) throw VitalsException(s"lexicon disabled!")
    _indexToStringMap.get(index) match {
      case None => throw VitalsException(s"index $index not found in lexicon")
      case Some(s) => s"lex_${index}_value"
    }
  }

  final override
  def nullityName(index: Int): String = {
    if (!_enabled) throw VitalsException(s"lexicon disabled!")
    _indexToStringMap.get(index) match {
      case None => throw VitalsException(s"index $index not found in lexicon")
      case Some(s) => s"lex_${index}_nullity"
    }
  }

}
