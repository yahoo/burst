/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.brio.reference.relation

import org.burstsys.felt.model.brio.reference.FeltBrioStdRef
import org.burstsys.felt.model.reference.FeltReference
import org.burstsys.felt.model.tree.code.{FeltCode, FeltCodeCursor, I, T}

/**
 * code generator for a [[FeltReference]]  to a reference vector Brio relation
 * ==Example==
 * <pre>  if( user.sessions == null) </pre>
 * '''NOTE:''' reference vectors are a special case because at this point the only thing you
 * can do is test them for `null`
 */
trait FeltBrioRefVecRef extends Any {

  self: FeltBrioStdRef =>

  protected
  def generateRefVecRefRead(implicit cursor: FeltCodeCursor): FeltCode = {
    s"""|
        |${T(this)}
        |${I}if ($instanceIsNull) { ${cursor.callScope.scopeNull} = true; } else {  ${cursor.callScope.scopeNull} = false; } // FELT-BRIO-REF-VEC""".stripMargin
  }


}
