/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.master.test.rest

import org.burstsys.master.test.support.BurstMasterSpecSupport

class InfoSpec extends BurstMasterSpecSupport {

  it should "fetch timezones" in {
    val timezones = fetchArrayFrom("/info/timezones").map { node => node.textValue() }
    timezones should contain allOf ("America/Chicago", "America/Los_Angeles", "Etc/UTC", "UTC")
  }

}
