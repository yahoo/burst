/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;

/**
 * The base level motif tree construct
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
@JsonTypeIdResolver(JsonSerde.class)
public abstract class NodeContext implements Node {

    @Nonnull
    @JsonIgnore
    private final NodeGlobal global;
    private NodeType ntype;
    private NodeLocation location;

    protected NodeContext(@Nonnull NodeGlobal global, NodeLocation location, NodeType ntype) {
        this(global, ntype);
        this.location = location;
        this.ntype = ntype;
    }

    protected NodeContext(@Nonnull NodeGlobal global, NodeType ntype) {
        this.global = global;
        this.ntype = ntype;
    }

    @Override
    public String explain() {
        return explain(0);
    }

    public String explain(int level) {
        return "";
    }

    protected String indent(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("\t");
        }
        return builder.toString();
    }

    protected String returnCleanString(StringBuilder builder) {
        return builder.toString().trim().replaceAll(" +", " ");
    }

    protected String endExplain(StringBuilder builder) {
        builder.append(')');
        builder.append('\n');
        return builder.toString();
    }

    protected StringBuilder startExplain(int level) {
        StringBuilder builder = new StringBuilder();
        builder.append(indent(level));
        String simpleName = getClass().getSimpleName();
        builder.append(simpleName, 0, simpleName.lastIndexOf("Context"));
        builder.append('(');
        return builder;
    }

    public final NodeLocation getLocation() {
        return location;
    }

    public final NodeGlobal getGlobal() {
        return global;
    }

    @Override
    public String exportAsJson() {
        try {
            ObjectMapper mapper = JsonMapper.builder()
                                            .disable(MapperFeature.AUTO_DETECT_GETTERS)
                                            .disable(MapperFeature.AUTO_DETECT_SETTERS)
                                            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                                            .enable(SerializationFeature.INDENT_OUTPUT).build();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("export failed", e);
        }
    }

    @Override
    public final NodeType getNodeType() {
        return ntype;
    }

}
