/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.tree

/**
 * We track the location of a parse node all the way to code generation. This means
 * we can give hopefully lexically rooted and perhaps helpful error messages
 * This means the language input source, not the code generated output source. Felt does not have a language
 * but assists languages (e.g. Hydra) in mapping source text to FELT tree nodes.
 */
trait FeltLocation {

  def global: FeltGlobal

  def lineIndex: Int

  def columnIndex: Int

  def contextualizedErrorMsg(msg: String): String
}

object FeltLocation {

  def apply(global: FeltGlobal, lineIndex: Int, columnIndex: Int): FeltLocation = {
    FeltLocationContext(isKnown = true, global = global, lineIndex = lineIndex: Int, columnIndex = columnIndex: Int)
  }

  def apply(): FeltLocation = {
    FeltLocationContext()
  }

}

private final case
class FeltLocationContext(isKnown: Boolean = false, global: FeltGlobal = null, lineIndex: Int = -1, columnIndex: Int = -1)
  extends FeltLocation with FeltContextualizer {

  override def toString: String = if (isKnown)
    s"Location(lineIndex=$lineIndex, columnIndex=$columnIndex)\n${contextualizedErrorMsg()}" else "UKNOWN_LOCATION"

  def source: String = global.source

  override def contextualizedErrorMsg(msg: String = ""): String = {
    if (!isKnown) {
      s"$msg: UNKNOWN_LOCATION "
    } else if (isContextualized(msg)) {
      msg
    } else {
      contextualize(lineIndex, columnIndex, msg)
    }
  }

}
