/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static java.lang.String.format;

public class JsonSerde extends TypeIdResolverBase {

    @Override
    public String idFromValue(Object value) {
        return ((NodeContext) value).getNodeType().getKey();
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        NodeType ntype = NodeType.byKey(id);
        if (ntype != null) {
            Class<? extends NodeContext> clazz = ntype.getClazz();
            if (clazz == null)
                throw new RuntimeException(format("id '%s' has no ntype implementation", id));
            return TypeFactory.defaultInstance().constructType(clazz);
        }
        throw new RuntimeException(format("id '%s' has no ntype", id));
    }


    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return null;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return null;
    }
}
