![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--

![](../../../burst-zap/doc/cubes.png "")
--

___Cubes___ are a Hydra language artifact that exposes a `collector ` data structure
 as provided in [Zap Cubes](../burst-zap/doc/cubes.md). They are central to
the Hydra analysis model and each subquery must have one as they are what provide for returning
 multidimensional tabular results to the query client.

Currently we enforce a single Cube per analysis sub-query though that is a semantic
not a syntactic distinction. The language _may be extended in the future_
to allow more than one Cube in a subquery, and for
subqueries to refer to Cubes defined in other subqueries within the same Analysis.

Each Cube can contain SubCubes that are used to define ___Join___ points between levels
of the Brio `object-tree` as the traversal unfolds.

###### Grammar
    cubeDeclaration:
        CUBE (identifier COLON)? path LB
            cubeLimit?
            (AGGREGATE LB aggregate* RB)?
            (DIMENSION LB dimension* RB)?
            subCubeDeclaration*
         RB ;
    
    subCubeDeclaration:
        CUBE path LB
            (AGGREGATE LB aggregate* RB)?
            (DIMENSION LB dimension* RB)?
            subCubeDeclaration*
         RB ;
         
    cubeLimit: LIMIT valueExpression ;

###### Examples
    sdfg



# Aggregations
###### Grammar

    aggregate:  (primitiveAggregate | topAggregate) ;
    
    primitiveAggregate: identifier COLON (SUM | UNIQUE | MAX | MIN) LSB valuePrimitiveTypeDeclaration RSB ;
    
    topAggregate: identifier COLON TOP LSB valuePrimitiveTypeDeclaration RSB LP valueExpression (SEP valueExpression (SEP valueExpression)? )? RP ;
###### Examples


# Dimensions
###### Grammar

    dimension:
        verbatimDimension | castDimension | splitDimension | enumDimension |
        calendarGrainDimension | DatetimeOrdinalDimension | datetimeGrainDimension
        ;
    
    verbatimDimension:  identifier COLON VERBATIM LSB valuePrimitiveTypeDeclaration RSB ;
    
    castDimension:  identifier COLON CAST LSB valuePrimitiveTypeDeclaration RSB ;
    
    splitDimension:  identifier COLON SPLIT  LSB valuePrimitiveTypeDeclaration RSB LP valueExpression (SEP valueExpression)* RP ;
    
    enumDimension:  identifier COLON ENUM  LSB valuePrimitiveTypeDeclaration RSB  LP valueExpression (SEP valueExpression)* RP ;
    
    calendarGrainDimension:  identifier COLON calendarGrainType LSB valuePrimitiveTypeDeclaration RSB ;

    DatetimeOrdinalDimension:  identifier COLON DatetimeOrdinalType LSB valuePrimitiveTypeDeclaration RSB ;

    datetimeGrainDimension:  identifier COLON datetimeGrainType LSB valuePrimitiveTypeDeclaration RSB ;
###### Examples

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------


