/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.flurry.provider.unity

import org.burstsys.brio.press.BrioPressInstance
import org.burstsys.brio.types.BrioTypes.BrioVersionKey

package object press {
  type UnityPressInstance = UnityPressInstanceV1

  trait UnityPressInstanceV1 extends BrioPressInstance {

    final override val schemaVersion: BrioVersionKey = 1

  }
}
