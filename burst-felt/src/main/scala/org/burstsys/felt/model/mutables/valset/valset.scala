/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.mutables

package object valset {

  trait FeltMutableValSetBuilder extends FeltMutableBuilder

  trait FeltMutableValSetProv extends FeltMutableProvider[FeltMutableValSet, FeltMutableValSetBuilder]

}
