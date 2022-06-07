/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.schemas;

import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.paths.Path;
import org.burstsys.motif.paths.UniversalPathBase;
import org.burstsys.motif.schema.model.MotifSchema;
import org.burstsys.motif.schema.model.SchemaReference;
import org.burstsys.motif.schema.model.context.SchemaRelationContext;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * Help with paths
 */
abstract public class SchemaPathBase extends UniversalPathBase {
    List<String> components;

    private final MotifSchema schema;

    String last() { return components.get(components.size()-1);}

    public List<String> getComponents() { return components; }

    protected SchemaPathBase(MotifSchema s, String fullPath) {
        assert(s != null);
        this.components = Arrays.asList(fullPath.split("\\."));
        this.schema = s;

        // check if last component is a key
        int lb = last().lastIndexOf('[');
        int rb = last().lastIndexOf(']');
        if (lb >= 0 && rb > 0 && rb > lb && rb == last().length()-1) {
            components.set(components.size()-1,last().substring(0, lb));
        }
    }

    public MotifSchema getSchema() {return schema;}

    public String getPathAsString() {
        return fullPathAsString(components);
    }

    public boolean isRoot() {
        // return components.size() == 1 && schema.getRootFieldName().equals(components.get(0));
       return components.size() == 1;
    }

    abstract public org.burstsys.motif.paths.Path getEnclosingStructure();

    abstract public org.burstsys.motif.paths.Path getParentStructure();

    public boolean sameOnPath(Path p) {
        return lowest(this.getEnclosingStructure(), p.getEnclosingStructure()) != null;
    }

    public boolean sameHigher(Path p) {
        org.burstsys.motif.paths.Path lp = lowest(this.getEnclosingStructure(), p.getEnclosingStructure());
        // it's only not lower and not equal (lowest returns the second argument only if they are not equal)
        return lp == p.getEnclosingStructure();
    }

    public boolean sameLower(Path p) {
        org.burstsys.motif.paths.Path lp = lowest(this.getEnclosingStructure(), p.getEnclosingStructure());
        // returns true if lower or equal
        return lp == this.getEnclosingStructure();
    }

    @Override
    public String toString() {
       return getPathAsString();
    }

    // Static Helpers

    /**
     * Return a path to the root
     */
    static public StructurePath rootPath(MotifSchema schema) {
        return (StructurePath) SchemaPathBase.formPath(schema, schema.getRootFieldName(), null);
    }

    /**
     * Given a schema and a path string form a path object.  The path can have map and index operations which
     * will be ignored.
     *
     */
    static public SchemaPathBase formPath(MotifSchema schema, String fullPath, String mapKey) {
        assert(schema != null);
        List<String> components = Arrays.asList(fullPath.split("\\."));
        String last = components.get(components.size()-1);

        // make sure the head is not an alias to the true root field name
        components.set(0, schema.getRootFieldName());

        // check if last component has a lookup by key
        boolean valueRef = false;
        boolean keyRef = false;

        // check if last component is a 'key' or 'value'
        if (last.toLowerCase().equals("key")) {
            // strip the last component
            components = components.subList(0, components.size()-1);
            keyRef = true;
        } else if (last.toLowerCase().equals("value")) {
            // strip the last component
            components = components.subList(0, components.size() - 1);
            valueRef = true;
        }

        //validate that this path is valid to the schema
        SchemaReference reference = schema.getStructurePathMap().get(fullPathAsString(components));
        if (reference != null) {
            if (mapKey != null) {
                throw new ParseException(format("instance reference structure '%s' in schema '%s' has map key '%s'",
                        reference.getReferenceType().getStructureName(), schema.getSchemaName(), mapKey)
                );
            }
            return new StructurePath(schema, fullPathAsString(components), reference);
        } else {
            String pathWithoutRelationAsString = pathWithoutRelationAsString(components);
            reference = schema.getStructurePathMap().get(pathWithoutRelationAsString);
            if (reference != null) {
                // this is a relation within a structure
                SchemaRelationContext relation = (SchemaRelationContext) reference.getReferenceType().getRelationNameMap().get(justRelationAsString(components));
                if (relation == null) {
                    throw new ParseException(
                            format("could not find relation '%s' in structure '%s' in schema '%s'",
                                    justRelationAsString(components), pathWithoutRelationAsString, schema.getSchemaName())
                    );
                }
                if (relation.getRelationType() == RelationType.VALUE_VECTOR) {
                    // it's  a value vector
                    if (mapKey != null) {
                        throw new ParseException(
                                format("value vector relation '%s' in structure '%s' in schema '%s' cannot have a map key '%s'",
                                        relation.getFieldName(), reference.getReferenceType().getStructureName(), schema.getSchemaName(), mapKey)
                        );
                    } else if (valueRef) {
                        // value reference
                        return new ValueVectorValuePath(schema, fullPathAsString(components), reference, relation);
                    } else
                        return new ValueVectorPath(schema, fullPathAsString(components), reference, relation);
                } else if (relation.getRelationType() == RelationType.VALUE_MAP) {
                    // it's  a map
                    if (mapKey != null) {
                        // map lookup
                        return new MapLookupPath(schema, fullPathAsString(components), reference, relation);
                    } else if (keyRef) {
                        // key reference
                        return new MapKeyPath(schema, fullPathAsString(components), reference, relation);
                    } else if (valueRef) {
                        // key reference
                        return new MapValuePath(schema, fullPathAsString(components), reference, relation);
                    } else
                        return new MapPath(schema, fullPathAsString(components), reference, relation);
                } else {
                    if (mapKey != null || valueRef || keyRef) {
                        throw new ParseException(
                                format("Relation '%s' in structure '%s' in schema '%s' cannot be used as a vector or map",
                                        relation.getFieldName(), reference.getReferenceType().getStructureName(), schema.getSchemaName())
                        );
                    }
                    return new RelationPath(schema, fullPathAsString(components), reference, relation);
                }
            } else {
                throw new ParseException(format("could not bind '%s' in schema '%s'", fullPathAsString(components), schema.getSchemaName()));
            }
        }
    }

    static private org.burstsys.motif.paths.Path lowest(org.burstsys.motif.paths.Path o, org.burstsys.motif.paths.Path t) {
        // first make sure we are looking at the enclosing structure since we don't care about fields
        SchemaPathBase one = (SchemaPathBase)o.getEnclosingStructure();
        SchemaPathBase two = (SchemaPathBase)t.getEnclosingStructure();

        int l = Math.max(one.components.size(), two.components.size());

        for (int i=0; i < l; i++) {
            if (i >= one.components.size())
                // we ran out of one components before two, so two is lower
                return two;
            else if (i >= two.components.size())
                // we ran out of two components before one, so one is lower
                return one;
            else if (!one.components.get(i).equals(two.components.get(i)))
                // the components  aren't equal so they can't be on the same path
                return null;
            // else they are equal so move on
        }
        // we finished for loop and everything was equal so they must be equal so either works, but return the first
        return one;
    }

    static String pathWithoutRelationAsString(List<String> components) {
        StringBuilder builder = new StringBuilder();
        for (String c: components.subList(0, components.size() - 1)) {
            builder.append(c);
            builder.append('.');
        }
        if (components.size() > 1)
            builder.deleteCharAt(builder.lastIndexOf("."));
        return builder.toString();
    }

    private static String justRelationAsString(List<String> components) {
        return components.get(components.size() - 1);
    }

    private static String fullPathAsString(List<String> components) {
        StringBuilder builder = new StringBuilder();
        for (String component : components) {
            builder.append(component);
            builder.append('.');
        }
        if (components.size() > 0)
            builder.deleteCharAt(builder.lastIndexOf("."));
        return builder.toString();
    }
}
