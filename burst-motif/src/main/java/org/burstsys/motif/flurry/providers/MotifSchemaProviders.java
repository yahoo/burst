/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.flurry.providers;

import org.burstsys.motif.schema.provider.BaseMotifSchemaProvider;
import org.burstsys.motif.schema.provider.MotifSchemaProvider;

import java.util.Collections;

/**
 * Register the Flurry schemas with the motif parser
 */
public class MotifSchemaProviders {

    public static final class QuoSchemaProvider extends BaseMotifSchemaProvider implements MotifSchemaProvider {
        public QuoSchemaProvider() {
            super("/org/burstsys/motif/flurry/schema/quo/quo_v3.motif", Collections.singletonList("quo"));
        }
    }

    public static final class UnitySchemaProvider extends BaseMotifSchemaProvider  implements MotifSchemaProvider {
        public UnitySchemaProvider() {
            super("/org/burstsys/motif/flurry/schema/unity/unity_v1.motif", Collections.singletonList("unity"));
        }
    }

    private MotifSchemaProviders() {
    }
}
