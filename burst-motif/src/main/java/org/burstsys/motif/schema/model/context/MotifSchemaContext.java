/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.schema.model.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.burstsys.motif.common.JsonSerde;
import org.burstsys.motif.common.NodeContext;
import org.burstsys.motif.common.NodeGlobal;
import org.burstsys.motif.common.NodeType;
import org.burstsys.motif.motif.tree.expression.Evaluation;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.symbols.PathSymbols;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.SchemaRelation;
import org.burstsys.motif.schema.model.SchemaStructure;
import org.burstsys.motif.schema.tree.ParseSchema;
import org.burstsys.motif.schema.tree.ParseStructure;

import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;

@JsonRootName("Schema")
public final class MotifSchemaContext extends NodeContext implements MotifSchema {

    @JsonProperty
    private final String schemaName;

    @JsonProperty
    private final String rootFieldName;

    @JsonProperty
    private final String rootStructureName;

    private final SchemaStructureContext rootStructure;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "ntype")
    @JsonTypeIdResolver(JsonSerde.class)
    @JsonProperty("structures")
    private final HashMap<String, SchemaStructureContext> structureNameMap = new HashMap<>();

    private final HashMap<String, SchemaReference> structurePathMap = new HashMap<>();
    private final HashMap<String, SchemaRelationContext> relationPathMap = new HashMap<>();
    private final ArrayList<SchemaStructureContext> structurePathList = new ArrayList<>();
    private final ArrayList<SchemaRelationContext> relationList = new ArrayList<>();

    public MotifSchemaContext(ParseSchema tree) {
        super(NodeGlobal.defaultNodeGlobal(), tree.getLocation(), NodeType.SCHEMA_MODEL);
        this.schemaName = tree.schemaName;
        this.rootFieldName = tree.root.fieldName;
        this.rootStructureName = tree.root.fieldType;
        // create symbol to structure map
        for (ParseStructure structure : tree.structures) {
            String structureName = structure.structureName;
            SchemaStructureContext s = new SchemaStructureContext(getGlobal(), structure.getLocation(), structureName, structure.fields);
            structurePathList.add(s);
            structureNameMap.put(structureName, s);
        }
        // bind in references to structures
        for (SchemaStructure structure : structureNameMap.values()) {
            for (SchemaRelation schemaRelation : structure.getRelationNumberMap().values()) {
                if (schemaRelation instanceof SchemaReferenceScalarContext) {
                    SchemaReferenceScalarContext node = (SchemaReferenceScalarContext) schemaRelation;
                    node.referenceType = structureNameMap.get(node.getReferenceTypeName());
                } else if (schemaRelation instanceof SchemaReferenceVectorContext) {
                    SchemaReferenceVectorContext node = (SchemaReferenceVectorContext) schemaRelation;
                    node.referenceType = structureNameMap.get(node.getReferenceTypeName());
                }
            }
        }
        // create fullPathAsString to structure map
        rootStructure = structureNameMap.get(rootStructureName);
        explore(rootFieldName, new SchemaRootVectorContext(getGlobal(), tree.getLocation(), rootStructure, rootFieldName));
    }

    @Override
    public String explain(int level) {
        StringBuilder builder = startExplain(level);
        builder.append('\'');
        builder.append(schemaName);
        builder.append('\'');
        builder.append('\n');
        builder.append(indent(level + 1));
        builder.append("root: ");
        builder.append('\'');
        builder.append(rootFieldName);
        builder.append('\'');
        builder.append(':');
        builder.append(rootStructureName);
        builder.append('\n');
        for (SchemaStructureContext structure : structurePathList) {
            builder.append(structure.explain(level + 1));
            builder.append(indent(level));
        }
        return endExplain(builder);
    }

    private void explore(String path, SchemaReference structure) {
        structurePathMap.put(path, structure);
        List<SchemaRelationContext> list = new ArrayList<>();
        for (SchemaRelation relation : structure.getReferenceType().getRelationNumberMap().values()) {
            list.add((SchemaRelationContext) relation);
        }
        Collections.sort(list);
        for (SchemaRelation schemaRelation : list) {
            String newPath = path + "." + schemaRelation.getFieldName();
            if (schemaRelation instanceof SchemaValueScalarContext) {
                SchemaValueScalarContext node = (SchemaValueScalarContext) schemaRelation;
                relationPathMap.put(newPath, node);
                relationList.add(node);
            } else if (schemaRelation instanceof SchemaValueVectorContext) {
                SchemaValueVectorContext node = (SchemaValueVectorContext) schemaRelation;
                relationPathMap.put(newPath, node);
                relationList.add(node);
            } else if (schemaRelation instanceof SchemaValueMapContext) {
                SchemaValueMapContext node = (SchemaValueMapContext) schemaRelation;
                relationPathMap.put(newPath, node);
                relationList.add(node);
            } else if (schemaRelation instanceof SchemaReferenceScalarContext) {
                SchemaReferenceScalarContext node = (SchemaReferenceScalarContext) schemaRelation;
                relationPathMap.put(newPath, node);
                explore(newPath, node);
                relationList.add(node);
            } else if (schemaRelation instanceof SchemaReferenceVectorContext) {
                SchemaReferenceVectorContext node = (SchemaReferenceVectorContext) schemaRelation;
                relationPathMap.put(newPath, node);
                explore(newPath, node);
                relationList.add(node);
            }
        }
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("schemaName", schemaName)
                .add("rootFieldName", rootFieldName)
                .add("rootStructureName", rootStructureName)
                .add("structureNameMap", structureNameMap)
                .add("structurePathMap", structurePathMap)
                .add("relationPathMap", relationPathMap)
                .add("relationList", relationList)
                .omitNullValues()
                .toString();
    }

    @Override
    public MotifSchema getSchema() {
        return this;
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public String getRootFieldName() {
        return rootFieldName;
    }

    @Override
    public String getRootStructureName() {
        return rootStructureName;
    }

    @Override
    public SchemaStructure getRootStructure() {
        return rootStructure;
    }

    @Override
    public Map<String, SchemaStructure> getStructureNameMap() {
        return Collections.unmodifiableMap(structureNameMap);
    }

    @Override
    public Map<String, SchemaReference> getStructurePathMap() {
        return Collections.unmodifiableMap(structurePathMap);
    }

    @Override
    public Map<String, SchemaRelation> getRelationPathMap() {
        return Collections.unmodifiableMap(relationPathMap);
    }

    @Override
    public List<SchemaStructure> getStructurePathList() {
        return Collections.unmodifiableList(structurePathList);
    }

    @Override
    public List<SchemaRelation> getRelationList() {
        return Collections.unmodifiableList(relationList);
    }

    @Override
    public String getName() {
        return schemaName;
    }

    @Override
    public Evaluation optimize(PathSymbols pathSymbols) {
        return this;
    }

    @Override
    public void bind(PathSymbols pathSymbols, Stack<Evaluation> stack) {
        pathSymbols.addCurrentScopeDefinition(Context.SCHEMA, this);

    }

    @Override
    public void validate(PathSymbols pathSymbols, Path scope, Stack<Evaluation> stack) {
    }

    @Override
    public String generateMotif(int level) {
        return explain(level);
    }
}
