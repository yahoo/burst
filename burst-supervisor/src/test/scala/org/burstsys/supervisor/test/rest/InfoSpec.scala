/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.test.rest

class InfoSpec extends SupervisorRestSpecBase {

  it should "fetch timezones" in {
    val timezones = fetchArrayFrom("/info/timezones").map { node => node.textValue() }
    timezones should contain allOf ("America/Chicago", "America/Los_Angeles", "Etc/UTC", "UTC")
  }

}
