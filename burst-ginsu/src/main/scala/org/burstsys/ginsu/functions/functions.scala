/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.ginsu

import org.burstsys.ginsu.functions.coerce.GinsuCoerceFunctions
import org.burstsys.ginsu.functions.datetime.GinsuDatetimeFunctions
import org.burstsys.ginsu.functions.group.GinsuGroupFunctions

package object functions {

  trait GinsuFunctions extends Any with GinsuCoerceFunctions with GinsuDatetimeFunctions with GinsuGroupFunctions

}
