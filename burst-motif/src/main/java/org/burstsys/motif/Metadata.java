/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.burstsys.motif.flurry.providers.MotifSchemaProviders;
import org.burstsys.motif.motif.tree.view.View;
import org.burstsys.motif.parser.schema.MotifSchemaParser;
import org.burstsys.motif.parser.statement.MotifStatementParser;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.provider.MotifSchemaProvider;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Metadata is a schema cache for Motif. It will automatically load any schemas on the classpath. Schema registrations
 * are case-insensitive, except for aliases provided by the detected {@link MotifSchemaProvider}s.
 * <p>
 * Please drop us a line if you find yourself needing to use this class directly, we'd be interested in hearing about your use case.
 */
public interface Metadata {

    /**
     * Get a new Metadata instance
     */
    static Metadata build() {
        return new MetadataContext();
    }

    @Deprecated
    void loadSchema(String name, String source);

    /**
     * Register a schema using the name defined in the schema
     *
     * @param schema the schema to register
     */
    void register(MotifSchema schema);

    /**
     * Register a schema using a provided alias
     *
     * @param schema the schema to register
     * @param alias  the alias to register the schema under
     */
    void register(MotifSchema schema, String alias);

    /**
     * Fetch a schema that has been previously registered
     *
     * @param alias alias of the schema (case-insensitive)
     * @return the schema or null if no schema has been registered under that alias
     */
    MotifSchema getSchema(String alias);

    @Deprecated
    View loadView(String name, String schemaName, String source);

    @Deprecated
    View getView(String name);

    /**
     * @return a set of all aliases used to register schemas
     */
    Set<String> allSchemaAliases();

    class MetadataContext implements Metadata {
        private static final Logger log = LogManager.getLogger(MetadataContext.class);
        private static final Map<String, MotifSchema> INTRENSIC_SCHEMAS = new ConcurrentHashMap<>();
        private static final AtomicBoolean INTRENSICS_LOADED = new AtomicBoolean(false);

        private final MotifSchemaParser schemaParser = MotifSchemaParser.build();
        private final MotifStatementParser motifParser = MotifStatementParser.build(this);
        private final Map<String, MotifSchema> schemas = new ConcurrentHashMap<>();
        private final Map<String, View> views = new ConcurrentHashMap<>();

        private MetadataContext() {
            loadIntrensicSchemas();
            schemas.putAll(INTRENSIC_SCHEMAS);
        }

        private static synchronized void loadIntrensicSchemas() {
            if (INTRENSICS_LOADED.get())
                return;

            String packages = System.getProperty("burst.motif.schemaPackage");
            if (packages == null || packages.isEmpty()) {
                packages = "org.burstsys";
            }
            MotifSchemaParser schemaParser = MotifSchemaParser.build();
            Reflections r = new Reflections(ConfigurationBuilder.build().forPackages(packages.split(",")));
            Set<Class<? extends MotifSchemaProvider>> subclasses = r.getSubTypesOf(MotifSchemaProvider.class);
            log.info("Found {} MotifSchemaProviders", subclasses.size());
            Set<Class<? extends MotifSchemaProvider>> providers =
                    subclasses.stream()
                              .filter(c -> {
                                  boolean abstractClass = Modifier.isAbstract(c.getModifiers());
                                  if (abstractClass) {
                                      log.info("Skipping abstract provider {}", c.getSimpleName());
                                  }
                                  return !abstractClass;
                              })
                              .collect(Collectors.toSet());
            for (Class<? extends MotifSchemaProvider> provider : providers) {
                String loaderName = provider.getSimpleName();
                log.info("Loading schema from '{}'", loaderName);
                try {
                    MotifSchemaProvider instance = provider.getDeclaredConstructor().newInstance();
                    MotifSchema schema = schemaParser.parseSchema(instance.getSchema());
                    if (schema == null) {
                        log.warn("Failed to parse intrensic schema from '{}'", loaderName);
                        continue;
                    }
                    for (String name : instance.getSchemaNames()) {
                        INTRENSIC_SCHEMAS.put(name, schema);
                    }
                } catch (Exception e) {
                    log.warn("Failed to load intrensic schema from '{}'", loaderName, e);
                }
            }

            INTRENSICS_LOADED.set(true);
        }

        @Deprecated
        @Override
        public void loadSchema(String name, String source) {
            MotifSchema schema = schemaParser.parseSchema(source);
            register(schema);
        }

        @Override
        public void register(MotifSchema schema) {
            register(schema, null);
        }

        @Override
        public void register(MotifSchema schema, String alias) {
            if (schema == null) {
                throw new IllegalStateException("Schema must not be null");
            }
            if (alias == null) {
                schemas.put(schema.getSchemaName().toLowerCase(), schema);
            } else {
                schemas.put(alias.toLowerCase(), schema);
            }
        }

        @Override
        public Set<String> allSchemaAliases() {
            return new HashSet<>(schemas.keySet());
        }

        @Override
        public MotifSchema getSchema(String alias) {
            if (alias == null) {
                return null;
            }
            return schemas.get(alias.toLowerCase());
        }

        @Deprecated
        @Override
        public View loadView(String name, String schemaName, String source) {
            return views.computeIfAbsent(name, key -> motifParser.parseView(schemaName, source));
        }

        @Deprecated
        @Override
        public View getView(String name) {
            return views.get(name);
        }
    }

}
