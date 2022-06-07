/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.types

import org.burstsys.vitals.errors.{VitalsError, VitalsException}

/**
  * a Burst Query Error caused by an invalid query definition or missing resource
  *
  * @param code
  * @param description
  */
sealed case class BrioError(code: Int, description: String) extends VitalsError

object BrioInvalidScalarValuePathError extends BrioError(1, "scalar value path")

object BrioInvalidInstancePathError extends BrioError(2, "instance path")

object BrioInvalidInstanceRelationError extends BrioError(11, "instance relation")

object BrioInvalidValueMapPathError extends BrioError(14, "value map path")

object BrioInvalidValueVectorPathError extends BrioError(15, "value vector path")

object BrioBadMapTypeError extends BrioError(16, "unsupported map type")

object BrioInvalidRelationError extends BrioError(17, "invalid relation")

final case class BrioException(error: BrioError, msg: String) extends VitalsException(error, msg)
