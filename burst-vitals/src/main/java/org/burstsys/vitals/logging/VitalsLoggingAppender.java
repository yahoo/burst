/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.vitals.logging;
/**/

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Plugin(name = "VitalsLogging", category = "Core", elementType = "appender", printObject = true)
@SuppressWarnings({"serial", "unused"})
public final class VitalsLoggingAppender extends AbstractAppender
{

    private VitalsLoggingAppender(final String name,
                                  final Filter filter,
                                  final Layout<? extends Serializable> layout,
                                  final boolean ignoreExceptions )
    {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

    /**
     */
    @PluginFactory
    public static VitalsLoggingAppender createAppender(
            // @formatter:off
            @PluginAttribute("name") final String name,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final String ignoreExceptions,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter
    )
    {
        if (name == null)
        {
            LOGGER.error("No name provided for Appender");
            return null;
        }

        if (layout == null)
        {
            layout = PatternLayout.newBuilder().withPattern("%m").withAlwaysWriteExceptions(true).withCharset(StandardCharsets.UTF_8).build();
        }

        final boolean ignoreExceptionsBool = Boolean.getBoolean(ignoreExceptions);

        return new VitalsLoggingAppender(
                name,
                filter, layout,
                ignoreExceptionsBool
        );
    }


    /**
     * Perform Appender specific appending actions.
     * @param event The Log event.
     */
    @Override
    public void append(final LogEvent event)
    {
        String loggerName;
        if (event.getLoggerName() == null)
            loggerName = "NoLoggerName";
        else
            loggerName = event.getLoggerName().trim().replace('$', ' ');
        String message;
        if (event.getMessage() == null)
            message = "NoMessage";
        else
            message = event.getMessage().toString();
        Level level = event.getLevel();
        String threadName;
        if (event.getThreadName() == null)
            threadName = "NoThreadName";
        else
            threadName = event.getThreadName();
        VitalsLogHandler.VitalsLogEvent logevent =
                new VitalsLogHandler.VitalsLogEvent(dateFormat.get().format(new Date(event.getTimeMillis())), level, threadName, loggerName, message);
        events.addLast(logevent);
        if (events.size() > bufferSize) {
            events.removeFirst();
        }
    }

    @Override
    public void stop() {
        super.stop();
    }
    private static final ThreadLocal<SimpleDateFormat> dateFormat =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S"));

    static final int bufferSize = 500;
    final static ConcurrentLinkedDeque<VitalsLogHandler.VitalsLogEvent> events = new ConcurrentLinkedDeque<>();

    public static List<VitalsLogHandler.VitalsLogEvent> getEvents(int lines) {
        return events.stream().limit(lines).collect(Collectors.toList());
    }

}
