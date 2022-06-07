//////////////////////////////////////////////////////////////////
// Subpart of Motif that provides a Brio like schema for external systems
//////////////////////////////////////////////////////////////////

grammar TypeGrammar;

import Tokens;

valueMapDecl : identifier ':' 'map' '[' simpleValueDatatype ',' simpleValueDatatype ']';

valueVectorDecl : identifier ':' 'vector' '[' simpleValueDatatype ']';

valueScalarDecl : identifier ':' simpleValueDatatype;

referenceScalarDecl : identifier ':' identifier;

referenceVectorDecl : identifier ':' 'vector' '[' identifier ']';

simpleValueDatatype : BOOLEAN_TYPE | BYTE_TYPE | SHORT_TYPE | INTEGER_TYPE | LONG_TYPE | DOUBLE_TYPE | STRING_TYPE ;

