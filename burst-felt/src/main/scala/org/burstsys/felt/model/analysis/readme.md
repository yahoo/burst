![Burst](../../../../../../../../../documentation/burst_h_small.png "")
![](../../../../../../../../doc/felt_small.png "")


# Felt Analysis
This is the top level object in a Felt semantic tree and represents
the complete behavioral analysis that is code generated into a
[Sweep](../sweep/readme.md). The analysis consists of one or more
[Frame](../frame/readme.md) declarations.

    analysis <analysis_name> ( <optional_parameters> ) {
        schema <schema_name>> 
        <frame> // one or more
    }

### parameters

### variables

### schema
        schema <schema_name>

#### schema extensions
    schema <schema_name> {
        <static_schema_path> -> <dynamic_schema_path>
    }

###### static schema paths
###### dynamic schema paths
