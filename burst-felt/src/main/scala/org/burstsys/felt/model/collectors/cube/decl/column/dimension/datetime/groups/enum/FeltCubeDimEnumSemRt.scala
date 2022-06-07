/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.cube.decl.column.dimension.datetime.groups.enum

import org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt

/**
 * Basic idea is that given a list of enumerated strings, and a test string to evaluate, return the matching
 * enumerated string or the last string ('other') if it doesn't match any of the preceding ones
 * TODO: is this more type specific than it needs to be?
 */
abstract
class FeltCubeDimEnumSemRt extends AnyRef
  with FeltCubeDimSemRt {

  final override protected val dimensionHandlesStrings: Boolean = false

  semanticType = ENUM_DIMENSION_SEMANTIC

}
