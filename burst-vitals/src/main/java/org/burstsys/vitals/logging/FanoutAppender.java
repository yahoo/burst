/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.logging;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.message.SimpleMessage;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This Appender allows the fans out a single logging event to be processed by other Appenders.
 */
@Plugin(name = "Fanout", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class FanoutAppender extends AbstractAppender {

    private final Configuration config;
    private final ConcurrentMap<String, AppenderControl> appenders = new ConcurrentHashMap<>();
    private final AppenderRef[] appenderRefs;
    private final StringLayout sLayout;

    private FanoutAppender(final String name, final Filter filter,
                                      final StringLayout layout,
                                      final boolean ignoreExceptions,
                                      final AppenderRef[] appenderRefs,
                                      final Configuration config) {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
        this.sLayout = layout;
        this.config = config;
        this.appenderRefs = appenderRefs;
    }

    @Override
    public void start() {
        for (final AppenderRef ref : appenderRefs) {
            final String name = ref.getRef();
            final Appender appender = config.getAppender(name);
            if (appender != null) {
                final Filter filter = appender instanceof AbstractAppender ?
                    ((AbstractAppender) appender).getFilter() : null;
                appenders.put(name, new AppenderControl(appender, ref.getLevel(), filter));
            } else {
                LOGGER.error("Appender " + ref + " cannot be located. Reference ignored");
            }
        }
        super.start();
    }

    /**
     * Modifies the event and pass to the subordinate Appenders.
     * @param event The LogEvent.
     */
    @Override
    public void append(LogEvent event) {
        // Format the incoming event message now
        LogEvent nLogEvent = new Log4jLogEvent.Builder(event).setMessage(new SimpleMessage(sLayout.toSerializable(event).trim())).build();
        for (final AppenderControl control : appenders.values()) {
            control.callAppender(nLogEvent);
        }
    }

    /**
     */
    @PluginFactory
    public static FanoutAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginAttribute("ignoreExceptions") final String ignore,
            @PluginElement("AppenderRef") final AppenderRef[] appenderRefs,
            @PluginConfiguration final Configuration config,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {

        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        if (name == null) {
            LOGGER.error("No name provided for FanoutAppender");
            return null;
        }
        if (layout == null)
        {
            layout = PatternLayout.newBuilder().withPattern("%m").withAlwaysWriteExceptions(true).withCharset(StandardCharsets.UTF_8).build();
        } else if (!(layout instanceof StringLayout)) {
            LOGGER.error("Layout must be a string layout");
            return null;
        }
        if (appenderRefs == null) {
            LOGGER.error("No appender references defined for FanoutAppender");
            return null;
        }
        return new FanoutAppender(name, filter, (StringLayout)layout, ignoreExceptions, appenderRefs, config);
    }
}
