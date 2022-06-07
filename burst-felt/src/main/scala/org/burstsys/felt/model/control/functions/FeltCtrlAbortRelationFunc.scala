/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.functions

/**
 * Stop processing all the current in scope instances associated with a given '''relationship'''
 * scalar vector, or map  'path' argument.
 * Do throw any in-scope transactional data
 * <ul>
 * <li>'''scope:''' the scope of this control verb is the highest level 'member' being processed at the provided path </li>
 * <li>'''cube data:''' current in scope changes are discarded and all subsequent updates are ignored </li>
 * <li>'''other collectors such as routes, tablets:''' current in scope changes are discarded and all subsequent updates are ignored </li>
 * <li>'''global or local variables:''' current in scope changes are discarded and all subsequent updates are ignored </li>
 * </ul>
 */
object FeltCtrlAbortRelationFunc {
  final val functionName: String = "abortRelation"
}

trait FeltCtrlAbortRelationFunc extends FeltCtrlVerbFunc {

  final override val nodeName = "felt-abort-relation-call"

  final override val functionName: String = FeltCtrlAbortRelationFunc.functionName

  final override val isRelation: Boolean = true
  final override val isAbort: Boolean = true

  final override val usage: String =
    s"""
       |usage: $functionName(<member_path>)
       |  stop processing all the current in scope instances associated with a given scalar, vector, or
       |   map 'relationship'  as specified by a path argument. Throw away any ''dirty'' (xactional) data collected in that scope
       |""".stripMargin

}
