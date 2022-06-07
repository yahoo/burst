/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.parser

import org.burstsys.vitals.logging._

/**
  * After antlr parses the hydra source, it presents a visitor API that can traverse the resulting semantic parse-tree.
  * This build code are the types that convert that parse-tree into a hydra semantic model during that traversal.
  */
package object builder extends VitalsLogger
