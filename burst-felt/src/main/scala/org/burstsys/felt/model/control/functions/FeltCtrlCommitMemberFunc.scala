/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.functions

/**
 * Stop processing of the current '''member''' of given scalar vector, or map relationship defined by a given 'path' argument.
 * Do '''not''' throw any in-scope transactional data
 * <ul>
 * <li>'''scope:''' the scope of this control verb is the highest level 'member' being processed at the provided path </li>
 * <li>'''cube data:''' current state is maintained but all subsequent updates are ignored </li>
 * <li>'''other collectors such as routes, tablets:''' current state is maintained but all subsequent updates are ignored </li>
 * <li>'''global or local variables:''' current state is maintained but all subsequent updates are ignored </li>
 * </ul>
 */
object FeltCtrlCommitMemberFunc {
  final val functionName: String = "commitMember"
}

trait FeltCtrlCommitMemberFunc extends FeltCtrlVerbFunc {

  final override val nodeName = "felt-commit-member-call"

  final override val functionName: String = FeltCtrlCommitMemberFunc.functionName

  final override val isRelation: Boolean = false
  final override val isAbort: Boolean = false

  final override val usage: String =
    s"""
       |usage:$functionName(<visit_path>)
       |  stop processing the given scalar, vector, or map member of the current scalar (collection of one), vector, or map collection
       |  Keep any ''dirty'' (xactional) data collected in that scope
      """.stripMargin

}
