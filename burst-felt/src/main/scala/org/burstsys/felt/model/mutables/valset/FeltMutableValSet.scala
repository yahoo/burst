/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.mutables.valset

import org.burstsys.felt.model.mutables.FeltMutable
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder

/**
 * =Felt Value Vector Mutable=
 * <hr/>
 * A subtype of [[org.burstsys.felt.model.mutables.FeltMutable]] representing a ''set'' of values.
 * There can only be a single instance of value-identity datum in a given set collection.
 * <br/><br/>
 * == BASIC SEMANTICS ==
 * <ol>
 * <li>sets are a variable size collection of scalar value atoms/primitives</li>
 * <li>all values are `unique`, i.e. duplicate insertions are ignored</li>
 * <li>a set supports `membership` tests i.e. return true if a value atom/primitive has been inserted</li>
 * </ol>
 * <br/><br/>
 * == NULLITY SEMANTICS ==
 * <ol>
 * <li>null value insertions are ignored</li>
 * </ol>
 * <br/><br/>
 * == Operations ==
 * <hr/>
 * <ol>
 * <li>INSERT A VALUE IF NOT ALREADY THERE</li>
 * <li>TEST FOR VALUE MEMBERSHIP</li>
 * <li>REMOVE A VALUE </li>
 * <li>CLEAR ALL VALUES</li>
 * </ol>
 * == Hydra Usages ==
 * {{{
 *    // as immutable set variable (populated via 'initializer')
 *    val v1:set[long] = set(6049337, 4498119)
 *
 *    // as mutable set variable (can be populated by 'initializer' and updated via write operations)
 *    var v2:set[long] = set()
 *
 *    // as parameter  (populated via 'initializer' or ''call invocation parameter'')
 *    hydra analysisA(p1:set[long] = set(6049337, 4498119))
 *
 * }}}
 */
trait FeltMutableValSet extends Any with FeltMutable {

}

object FeltMutableValSet {

  case class FeltValSetMutableBuilder() extends TeslaPartBuilder {
    override def defaultStartSize: TeslaMemorySize = ???
  }

}
