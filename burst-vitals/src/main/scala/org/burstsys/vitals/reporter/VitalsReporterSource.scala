/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter

/**
 * Designate a source for reporters in a package.
 *
 * This trait is expected to be applied to a package object
 */
trait VitalsReporterSource {

  /**
   * List all reporters to be registered with the reporting system
   */
  def reporters: Array[VitalsReporter]

}
