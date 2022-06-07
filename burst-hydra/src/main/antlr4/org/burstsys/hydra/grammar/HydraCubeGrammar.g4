//////////////////////////////////////////////////////////////////
// Antlr Grammar for Hydra Cubes
//////////////////////////////////////////////////////////////////

grammar HydraCubeGrammar;

import HydraLexerGrammar, HydraExpressionGrammar;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// declarations
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// a multidimensional table collector schema
cubeDeclaration:
    CUBE pathExpression LB
        cubeProperty*
        cubeAggregations?
        cubeDimensions?
        subCubeDeclaration*
     RB ;

// cubes can be recursively designed
subCubeDeclaration:
    CUBE pathExpression LB
        cubeAggregations?
        cubeDimensions?
        subCubeDeclaration*
     RB ;

cubeAggregations:
    (AGGREGATES LB aggregate* RB)
    ;

cubeDimensions:
    (DIMENSIONS LB dimension* RB)
    ;

cubeProperty:
    // maximum number or rows per top level cube
    LIMIT ASSIGN expression
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// cube dimensions
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// the dimension 'key' for a row in a cube table 'group by'
dimension:
    verbatimDimension | castDimension | splitDimension | enumDimension |
    datetimeGrainDimension | datetimeOrdinalDimension | datetimeDurationDimension
    ;

// a cube dimension that does not implement any form of bucketing/grouping
verbatimDimension:  identifier COLON VERBATIM LSB valuePrimitiveTypeDeclaration RSB ;

// a cube dimension that converts/coerces the value
castDimension:  identifier COLON CAST LSB valuePrimitiveTypeDeclaration RSB ;

// a cube dimension that buckets values based on a set of range boundaries
splitDimension:  identifier COLON SPLIT  LSB valuePrimitiveTypeDeclaration RSB LP expression (SEP expression)* RP ;

// a cube dimension that buckets values based on a set of enumerated values
enumDimension:  identifier COLON ENUM  LSB valuePrimitiveTypeDeclaration RSB  LP expression (SEP expression)* RP ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// truncates epoch time to a particular calendar 'grain'
////////////////////////////////////////////////////////////////////////////////////////////////////////////

datetimeGrainDimension:  identifier COLON datetimeGrainType LSB valuePrimitiveTypeDeclaration RSB ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// buckets epoch time to an calendar correlated ordinal
////////////////////////////////////////////////////////////////////////////////////////////////////////////

datetimeOrdinalDimension:  identifier COLON datetimeOrdinalType LSB valuePrimitiveTypeDeclaration RSB ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// truncates epoch time to a particular time 'grain'
////////////////////////////////////////////////////////////////////////////////////////////////////////////

datetimeDurationDimension:  identifier COLON datetimeDurationType LSB valuePrimitiveTypeDeclaration RSB ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// cube aggregates
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// the aggregate 'value' in a cube table row
aggregate:  (primitiveAggregate | takeAggregate) ;

// simple aggregation type that takes no parameters
primitiveAggregate:
    identifier COLON MAX LSB valuePrimitiveTypeDeclaration RSB          #maxAggregate
    |   identifier COLON MIN LSB valuePrimitiveTypeDeclaration RSB      #minAggregate
    |   identifier COLON PROJECT LSB valuePrimitiveTypeDeclaration RSB  #projectAggregate
    |   identifier COLON SUM LSB valuePrimitiveTypeDeclaration RSB      #sumAggregate
    |   identifier COLON UNIQUE LSB valuePrimitiveTypeDeclaration RSB   #uniqueAggregate
    ;

// aggregation that groups/sorts/truncates cube table rows based on the value
takeAggregate:
    identifier COLON TOP LSB valuePrimitiveTypeDeclaration RSB LP expression (SEP expression (SEP expression)? )? RP  #topAggregate
    |   identifier COLON BOTTOM LSB valuePrimitiveTypeDeclaration RSB LP expression (SEP expression (SEP expression)? )? RP  #bottomAggregate
    ;
