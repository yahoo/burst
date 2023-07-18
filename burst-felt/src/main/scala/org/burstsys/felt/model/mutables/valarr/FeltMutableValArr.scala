/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.mutables.valarr

import org.burstsys.felt.model.mutables.FeltMutable
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder

/**
 * ==Felt Value Array Mutable==
 * <hr/>
 * A subtype of [[org.burstsys.felt.model.mutables.FeltMutable]]
 * representing an ordered ''array'' of values
 * <br/><br/>
 * == Basics ==
 * <ol>
 * <li>arrays are variable size ordered list of scalar atoms (primitive values)</li>
 * <li>arrays are read and written by specifying an ''ordinal'' (index into the variable size list)</li>
 * <li>arrays can be initialized by assigning a array ''literal'' to them at declaration</li>
 * <li>arrays can be assigned (reinitialized) to an array ''literal'' at any other point as well</li>
 * </ol>
 * == Nullity Rules ==
 * <ol>
 * <li>all values at all ordinal positions are considered to be ''null'' before initialization</li>
 * <li>any value at any ordinal can be set to ''null'' at any time</li>
 * </ol>
 * <br/><br/>
 * == Operations ==
 * <hr/>
 * <ol>
 * <li>WRITE SCALAR VALUE OR NULL TO THE ARRAY AT A GIVEN ORDINAL </li>
 * <li>WRITE SCALAR VALUE OR NULL FROM THE ARRAY AT A GIVEN ORDINAL </li>
 * </ol>
 * <br/><br/>
 * == Hydra Usages ==
 * {{{
 *    // as immutable set variable (populated via 'initializer')
 *    val v1:array[long] = array(6049337, 4498119) // using an array() literal construct...
 *
 *    // after declaration you can still use an array literal
 *    v2 = array() // clear the array
 *
 *    // as parameter  (populated via 'initializer' or ''call invocation parameter'')
 *    hydra analysisA(p1:array[long] = array(6049337, 4498119))
 * }}}
 *
 */
trait FeltMutableValArr extends Any with FeltMutable {

}

object FeltMutableValArr {

  case class FeltValArrayMutableBuilder() extends TeslaPartBuilder {
    override def defaultStartSize: TeslaMemorySize = 0
  }

}
