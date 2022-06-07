//////////////////////////////////////////////////////////////////
// Antlr Grammar for Brio schema (extended Motif Schema)
//////////////////////////////////////////////////////////////////

grammar BrioSchemaGrammar;

schemaClause :  'schema' Identifier '{' versionClause rootClause structureClause+  '}' EOF ;

versionClause : 'version' ':' Number ;

rootClause : 'root' ')'  Identifier  ':' Identifier ;

structureClause : 'structure' Identifier ('extends' Identifier)? '{' relation+ '}' ;

relation
    : valueMapRelation
    | valueVectorRelation
    | valueScalarRelation
    | referenceScalarRelation
    | referenceScalarRelation
    | referenceVectorRelation
    ;

valueMapRelation : Number ')' Identifier ':' 'map' '[' valueDatatype ',' valueDatatype ']' classifier*;

valueVectorRelation : Number ')' Identifier ':' 'vector' '[' valueDatatype ']' classifier*;

valueScalarRelation : Number ')' Identifier ':' valueDatatype classifier*;

referenceScalarRelation : Number ')' Identifier ':' Identifier classifier*;

referenceVectorRelation : Number ')' Identifier ':' 'vector' '[' Identifier ']' classifier*;

classifier : 'key' | 'ordinal' ;

valueDatatype : simpleValueDatatype | elasticValueDatatype | lookupValueDatatype ;

simpleValueDatatype : 'boolean' | 'byte' | 'short' | 'integer' | 'long' | 'double' | 'string' ;

elasticValueDatatype : 'elastic' '(' Number  (',' Number)? (',' Identifier)? ')' ;

lookupValueDatatype : 'lookup' '(' Number ',' Identifier ')' ;

//////////////////////////////////////////////////////////
// lexer
//////////////////////////////////////////////////////////

Identifier
    : Letter LetterOrDigit*
    |  '"' Identifier '"'
    |  '\'' Identifier '\''
    ;

Number : Digit+ ;

fragment
Digit :   [0-9] ;

fragment
Letter
    :   [a-zA-Z$_] // these are the "letters" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;

fragment
LetterOrDigit
    :   [a-zA-Z0-9$_] // these are the "letters or digits" below 0x7F
    |   // covers all characters above 0x7F which are not a surrogate
        ~[\u0000-\u007F\uD800-\uDBFF]
    |   // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
        [\uD800-\uDBFF] [\uDC00-\uDFFF]
    ;

//////////////////////////////////////////////////////////
// White Space and Comments
//////////////////////////////////////////////////////////

WS  :  [ \t\r\n\u000C]+ -> skip ;

COMMENT :   '/*' .*? '*/' -> channel(HIDDEN) ;

LINE_COMMENT :   '//' ~[\r\n]* -> channel(HIDDEN) ;
