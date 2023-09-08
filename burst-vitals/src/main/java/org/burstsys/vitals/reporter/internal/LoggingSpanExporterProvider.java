/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.burstsys.vitals.reporter.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.burstsys.vitals.reporter.TrekLoggingSpanExporter;
import org.jetbrains.annotations.NotNull;

/**
 * {@link SpanExporter} SPI implementation for {@link org.burstsys.vitals.reporter.TrekLoggingSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("unused")
public class LoggingSpanExporterProvider implements ConfigurableSpanExporterProvider {
  @Override
  public SpanExporter createExporter(@NotNull ConfigProperties config) {
    return TrekLoggingSpanExporter.create();
  }

  @Override
  public String getName() {
    return "trek-logging";
  }
}
