//////////////////////////////////////////////////////////////////
// Subpart of Motif that provides a Brio like schema for external systems
//////////////////////////////////////////////////////////////////

grammar MotifSchemaGrammar;
import Tokens, TypeGrammar;

schemaClause :  'schema' identifier '{' versionClause rootClause structureClause+  '}' EOF ;

versionClause : 'version' ':' INTEGER_LITERAL ;

rootClause : 'root' ')'  identifier  ':' identifier ;

structureClause : 'structure' identifier ('extends' identifier)? '{' relation+ '}' ;

relation
    : valueMapRelation
    | valueVectorRelation
    | valueScalarRelation
    | referenceScalarRelation
    | referenceVectorRelation
    ;

valueMapRelation : INTEGER_LITERAL ')' valueMapDecl classifier*;

valueVectorRelation : INTEGER_LITERAL ')' valueVectorDecl classifier*;

valueScalarRelation : INTEGER_LITERAL ')' valueScalarDecl classifier*;

referenceScalarRelation : INTEGER_LITERAL ')' referenceScalarDecl classifier*;

referenceVectorRelation : INTEGER_LITERAL ')' referenceVectorDecl classifier*;

classifier : 'key' | 'ordinal' ;

