/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.reference.relation

import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}

/**
 * code generator for a [[FeltReference]] to a reference scalar Brio relation
 * ==Example==
 * <pre>  if( user.application == null) </pre>
 * '''NOTE:''' reference scalars are a special case because at this point the only
 * thing you can do is test them for `null`
 */
trait FeltBrioRefScalRef extends Any {

  self: FeltBrioStdRef =>

  protected
  def generateRefScalRefRead(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${T(this)}
        |${I}if ($instanceIsNull) { ${cursor.callScope.scopeNull} = true; } else {  ${cursor.callScope.scopeNull} = false; } // FELT-BRIO-REF-SCAL""".stripMargin
  }

}
