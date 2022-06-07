/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.brio.blob._
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.sweep._


/** ==The Lattice ==
 * The ''lattice'' package contains types associated with
 * the traversal of a [[BrioBlob]]. The basic job of
 * an [[FeltAnalysisDecl]] is to be
 * code generated into a [[FeltSweep]] which then does a depth
 * first traversal of the blob via the lattice (zero GC) scan operations.
 */
package object lattice extends FeltLatSymbols {


}
