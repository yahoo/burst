/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.provider;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public abstract class BaseMotifSchemaProvider implements MotifSchemaProvider {

    protected String schema;
    protected String[] schemaNames;

    /**
     * Create a schema provider that loads a schema file from the classpath.
     *
     * @param resourcePath the location of the schema file on the classpath
     * @param names        the aliases that can be used for this schema
     */
    protected BaseMotifSchemaProvider(String resourcePath, List<String> names) {
        this(resourcePath, names.toArray(new String[0]));
    }

    /**
     * Create a schema provider that loads a schema file from the classpath.
     *
     * @param resourcePath the location of the schema file on the classpath
     * @param names        the aliases that can be used for this schema
     */
    protected BaseMotifSchemaProvider(String resourcePath, String[] names) {
        schemaNames = names;
        schema = getTextFromResource(resourcePath);
    }

    public static String getTextFromResource(String resourcePath) {
        try (InputStream stream = BaseMotifSchemaProvider.class.getResourceAsStream(resourcePath);
             Scanner s = new Scanner(stream, "UTF-8")
        ) {
            return s.useDelimiter("\\A").next();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load schema file: '" + resourcePath + "'", e);
        }
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String[] getSchemaNames() {
        return schemaNames;
    }
}
