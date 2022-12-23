/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy.usecase

/**
 * '''Alloy''' is a one stop shop for writing unit tests. A properly designed '''Alloy''' test can be designed for, written in,
 * and executed in the origin lower level subsystem, and then successfully and meaningfully executed at any point
 * up the stack including ultimately at the 'system test' layer where the exact same test can be applied to
 * and exercise the full set of public APIs in a full featured container topology.<p/>
 * == Requirements ==<hr/>
 * <ol>
 * <li>An '''Alloy''' test is coded and executed in any source module at any point in the stack above and in the `burst-unit` module</li>
 * <li>An '''Alloy''' test van be executed at any layer above that source module including the `burst-system-integration-tests` module for a full system test</li>
 * <li>If an '''Alloy''' test is ''parameterizable'' it must be of a standard object tree JSON parameter input form </li>
 * <li>If an '''Alloy''' test is against a ''data view'', that view must be available outside the source module</li>
 * <li>An '''Alloy''' test is ''data validated'' it must be against a common  object tree JSON data model </li>
 * <li>An '''Alloy''' test must must validate (success/fail) against a common 'correctness' test </li>
 * <li>All language/subsystem modules must port all tests to '''Alloy''' where sensible and tractable </li>
 * <li>'''Alloy''' tests should be designed so they run fast so they can be run more than once efficiently. This includes
 * common data loading, and other initialization using batch modality</li>
 * </ol>
 */
abstract class AlloyUseCase {

}
