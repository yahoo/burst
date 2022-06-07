/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.api

import scala.language.postfixOps

/**
  * [retry-budgets](https://finagle.github.io/blog/2016/02/08/retry-budgets/)
  *
  * This is for thrift clients who want to establish a retry model other than the
  * finagle thift defaults.
  */
object BurstApiRetries {

  /**
    * This is the way that finagle retry modeling works.
    * <hr/>
    * '''ttl''' Deposits created by `deposit()` expire after
    * approximately `ttl` time has passed. Must be `>= 1 second`
    * and `<= 60 seconds`.
    * <hr/>
    * '''minRetriesPerSec''' the minimum rate of retries allowed in order to
    * accommodate clients that have just started issuing requests as well as clients
    * that do not issue many requests per window.
    * Must be non-negative and if `0`, then no reserve is given.
    * <hr/>
    * '''percentCanRetry''' the percentage of calls to `deposit()` that can be
    * retried. This is in addition to any retries allowed for via `minRetriesPerSec`.
    * Must be >= 0 and <= 1000. As an example, if `0.1` is used, then for every
    * 10 calls to `deposit()`, 1 retry will be allowed. If `2.0` is used then every
    * `deposit` allows for 2 retries.
    * <hr/>
    * '''nowMillis''' the current time in milliseconds since the epoch.
    * The default of [[com.twitter.util.Stopwatch.systemMillis]] is generally appropriate,
    * though using [[com.twitter.util.Stopwatch.timeMillis]] is useful for well behaved tests
    * so that you can control [[com.twitter.util.Time]].
    */
  /*

val budget = RetryBudget(
  ttl = 10 seconds,
  minRetriesPerSec = 5,
  percentCanRetry = 0.1
)

Thrift.client.withRetryBudget(budget = budget)

*/

}
