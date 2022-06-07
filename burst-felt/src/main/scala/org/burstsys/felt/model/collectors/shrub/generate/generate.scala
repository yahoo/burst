/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.shrub

import org.burstsys.felt.model.tree.{FeltNode, FeltTreeRules}

package object generate extends FeltShrubSymbols {

  final implicit
  class FeltShrubRules(node: FeltNode) extends FeltTreeRules {

  }

}
