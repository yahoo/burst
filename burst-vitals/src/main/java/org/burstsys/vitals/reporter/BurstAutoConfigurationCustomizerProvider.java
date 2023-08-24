package org.burstsys.vitals.reporter;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.logging.Logger;


@SuppressWarnings("unused")
@AutoService(AutoConfigurationCustomizerProvider.class)
public class BurstAutoConfigurationCustomizerProvider implements AutoConfigurationCustomizerProvider {

    private static final Logger logger = Logger.getLogger(BurstAutoConfigurationCustomizerProvider.class.getName());

    @Override
    @ParametersAreNonnullByDefault
    public void customize(AutoConfigurationCustomizer autoConfigurationCustomizer) {
        logger.info("BurstAutoConfigurationCustomizerProvider will customize otel");
        autoConfigurationCustomizer.addTracerProviderCustomizer(this::addBurstTracers);
    }

    private SdkTracerProviderBuilder addBurstTracers(SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
//        if (!config.getList("otel.traces.exporter", Collections.emptyList()).contains("trek"))
//            return tracerProvider;
        logger.info("TrekLoggingSpanExporter is enabled");
        return tracerProvider.addSpanProcessor(TrekSpanProcessor.create(TrekLoggingSpanExporter.create()));
    }

}
