
//////////////////////////////////////////////////////////////////
// Subpart of Motif that provides non EQL filter syntax
//////////////////////////////////////////////////////////////////
grammar MotifCommonGrammar;

import Tokens, EqlExpressionGrammar, TypeGrammar;

// Data sources
schemaSource:
    SCHEMA identifier
    ;

catalogSource:
    identifier '.' identifier       #viewSource
    | identifier                    #domainSource
    ;

segmentSource:
    SEGMENT identifier (parameters=valueExpressionList)?
    ;

funnelSource:
    FUNNEL identifier (parameters=valueExpressionList)?
    ;

source:
    schemaSource |
    segmentSource |
    funnelSource
    ;

sourceList:
    namedSource ( ',' namedSource )*
    ;

namedSource:
    source ( AS identifier ) ?
    ;

rangeArguments:
    '(' valueDeclaration ( ',' valueDeclaration )* ')'
    ;

tagList:
    '[[' identifier ( ',' identifier )* ']]'
    ;

valueDeclaration:
    valueScalarDecl | valueVectorDecl
    ;

limitDeclaration:
    LIMIT limitValue=constant
    ;
