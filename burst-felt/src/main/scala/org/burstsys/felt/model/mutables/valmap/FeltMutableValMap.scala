/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.mutables.valmap

import org.burstsys.felt.model.mutables.FeltMutable
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.part.TeslaPartBuilder

/**
 * ==Felt Value Map Mutable==
 * <hr/>
 * A subtype of [[org.burstsys.felt.model.mutables.FeltMutable]]
 * representing a map of key -> value atom/primitive associations
 * <br/><br/>
 * == BASIC SEMANTICS ==
 * <ol>
 * <li>a map is a variable size list of scalar value '''keys''' to scalar value '''values''' ''associations''</li>
 * <li>if a map is given a `key` it will return the corresponding `value`</li>
 * <li>if a map is given a key '''and''' a value it `add`, `delete`, or `modify` that association</li>
 * <li>maps can initialized using a map ''literal'' in the declaration and at any other point</li>
 * </ol>
 * <br/><br/>
 * == Nullity SEMANTICS ==
 * <ol>
 * <li>keys cannot be `null` - if a null key is specified the operation is considered a NOOP</li>
 * <li>values can be `null` but setting a key to null is equivalent to removing the association</li>
 * <li>absence of a key is interpreted as equivalent to a `null` value</li>
 * </ol>
 * <br/><br/>
 * == Operations ==
 * <hr/>
 * <ol>
 * <li>WRITE A KEY, VALUE ASSOCIATION TO THE MAP </li>
 * <li>READ A VALUE ASSOCIATION GIVEN A KEY </li>
 * <li>TEST FOR THE PRESENCE OF A KEY/VALUE ASSOCIATION </li>
 * </ol>
 * == Hydra Usages ==
 * {{{
 *    // as immutable set variable (populated via 'initializer')
 *    val v1:map[long, long] = map(6049337 -> 0, 4498119  -> -1)
 *
 *    // as mutable set variable (can be populated by 'initializer' and updated via write operations)
 *    var v2:map[long] = map()
 *
 *    // as parameter  (populated via 'initializer' or ''call invocation parameter'')
 *    hydra analysisA(p1:map[long] = array(6049337 -> 0, 4498119 -> -1))
 *
 * }}}
 */
trait FeltMutableValMap extends Any with FeltMutable {

}

object FeltMutableValMap {

  case class FeltValMapMutableBuilder() extends TeslaPartBuilder {
    override def defaultStartSize: TeslaMemorySize = ???
  }

}
