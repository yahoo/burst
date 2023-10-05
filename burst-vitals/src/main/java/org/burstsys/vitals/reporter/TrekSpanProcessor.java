/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.reporter;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the {@link SpanProcessor} that converts the {@link ReadableSpan} to {@link
 * SpanData} and passes it directly to the configured exporter. {@link TrekSpanProcessor} is meant
 * for use with the {@link TrekLoggingSpanExporter}.
 *
 * <p>This processor will cause all spans to be exported directly as they finish, meaning each
 * export request will have a single span. Most backends will not perform well with a single span
 * per request so unless you know what you're doing, strongly consider using {@link
 * BatchSpanProcessor} instead, including in special environments such as serverless runtimes.
 */
public final class TrekSpanProcessor implements SpanProcessor {

    private static final Logger logger = Logger.getLogger(TrekSpanProcessor.class.getName());

    private final SpanExporter spanExporter;
    private final boolean onlySampled;
    private final Set<CompletableResultCode> pendingExports =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Returns a new {@link TrekSpanProcessor} which exports spans to the {@link SpanExporter}
     * synchronously.
     *
     * <p>This processor will cause all spans to be exported directly as they finish, meaning each
     * export request will have a single span. Most backends will not perform well with a single span
     * per request so unless you know what you're doing, strongly consider using {@link
     * BatchSpanProcessor} instead, including in special environments such as serverless runtimes.
     * {@link TrekSpanProcessor} is generally meant to for logging exporters only.
     */
    public static SpanProcessor create(SpanExporter exporter) {
        requireNonNull(exporter, "exporter");
        return new TrekSpanProcessor(exporter, /* onlySampled= */ true);
    }

    TrekSpanProcessor(SpanExporter spanExporter, boolean onlySampled) {
        this.spanExporter = requireNonNull(spanExporter, "spanExporter");
        this.onlySampled = onlySampled;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onStart(Context parentContext, ReadWriteSpan span) {
        if (onlySampled && !span.getSpanContext().isSampled()) {
            return;
        }
        StringBuilder sb = new StringBuilder(256);
        SpanData spanData = span.toSpanData();
        span.getLatencyNanos();
        sb.append("----> VITALS_TREK_BEGIN(")
                .append(" tmark=").append(spanData.getName()).append(",")
                .append(" traceid=").append(spanData.getTraceId()).append(",")
                .append(" spanid=").append(spanData.getSpanId()).append(",")
                .append(" parentspanid=").append(spanData.getParentSpanId()).append(",")
                .append(spanData.getAttributes())
                .append(')');
        logger.log(Level.INFO, sb.toString());
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(@NotNull ReadableSpan span) {
        if (onlySampled && !span.getSpanContext().isSampled()) {
            return;
        }
        try {
            List<SpanData> spans = Collections.singletonList(span.toSpanData());
            CompletableResultCode result = spanExporter.export(spans);
            pendingExports.add(result);
            result.whenComplete(
                    () -> {
                        pendingExports.remove(result);
                        if (!result.isSuccess()) {
                            logger.log(Level.FINE, "Exporter failed");
                        }
                    });
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Exporter threw an Exception", e);
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    @Override
    public CompletableResultCode shutdown() {
        if (isShutdown.getAndSet(true)) {
            return CompletableResultCode.ofSuccess();
        }
        CompletableResultCode result = new CompletableResultCode();

        CompletableResultCode flushResult = forceFlush();
        flushResult.whenComplete(
                () -> {
                    CompletableResultCode shutdownResult = spanExporter.shutdown();
                    shutdownResult.whenComplete(
                            () -> {
                                if (!flushResult.isSuccess() || !shutdownResult.isSuccess()) {
                                    result.fail();
                                } else {
                                    result.succeed();
                                }
                            });
                });

        return result;
    }

    @Override
    public CompletableResultCode forceFlush() {
        return CompletableResultCode.ofAll(pendingExports);
    }

    @Override
    public String toString() {
        return "SimpleSpanProcessor{" + "spanExporter=" + spanExporter + '}';
    }
}
