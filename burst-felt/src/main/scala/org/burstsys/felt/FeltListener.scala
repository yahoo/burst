/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt

import org.burstsys.felt.compile.FeltClassName
import org.burstsys.felt.compile.artifact.{FeltArtifactKey, FeltArtifactTag}

/**
 * event listener for felt service events
 */
trait FeltListener extends Any {

  /**
   * called when a sweep is cleaned (class artifacts removed from classloader)
   *
   * @param key
   * @param tag
   */
  def onFeltSweepClean(key: FeltArtifactKey, tag: FeltArtifactTag): Unit

  def onFeltSweepGenerate(key: FeltArtifactKey): Unit

  def onFeltAddToClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, className: FeltClassName, byteCount: Int): Unit

  def onFeltDeleteFromClassLoader(key: FeltArtifactKey, tag: FeltArtifactTag, className: FeltClassName): Unit

}
