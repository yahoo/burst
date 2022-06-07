/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.functions

/**
 * Stop processing of the current '''member''' of given scalar vector, or map relationship defined by a given 'path' argument.
 * Do throw any in-scope transactional data
 * <ul>
 * <li>'''scope:''' the scope of this control verb is the highest level 'member' being processed at the provided path </li>
 * <li>'''cube data:''' current in scope changes are discarded and all subsequent updates are ignored </li>
 * <li>'''other collectors such as routes, tablets:''' current in scope changes are discarded and all subsequent updates are ignored </li>
 * <li>'''global or local variables:''' current in scope changes are discarded and all subsequent updates are ignored </li>
 * </ul>
 */
object FeltCtrlAbortMemberFunc {
  final val functionName: String = "abortMember"
}

trait FeltCtrlAbortMemberFunc extends FeltCtrlVerbFunc {

  final override val nodeName = "felt-abort-member-call"

  final override val functionName: String = FeltCtrlAbortMemberFunc.functionName

  final override val isRelation: Boolean = false
  final override val isAbort: Boolean = true

  final override val usage: String =
    s"""
       |usage: $functionName(<member_path>)
       |  stop processing the current 'member' of the given scalar, vector, or map member as specified by a path argument.
       |  Throw away any ''dirty'' (xactional) data collected in that scope
       |""".stripMargin

}
