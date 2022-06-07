![Burst](../../doc/burst_small.png "")

_Motif:_ ```'recurring salient thematic element...'```

# Motif Schema
Motif includes its own schema language, based on and translateable from the standard Brio Schema.
 It is simplified and has an exportable [antlr4](http://www.antlr.org/) parser based grammar. 

* [Motif Schema Antlr Grammar](../src/main/antlr4/org/burstsys/motif/MotifSchemaGrammar.g4)
* [Motif Schema Canonical Example](../../burst-schema/src/main/resources/org/burstsys/schema/quo/quo_v3.schema)


## Basics

### Comments
C standard line `// comment` and block style `/* comment */` comments are supported. Line ends are flexible and no 
semicolons are required to terminate clauses.

#### Schema Clause

    schema Quo  {
        root : user : User
        ...
    }

This is the top level schema construct that names the query `'Quo'` and provide the __root__ clause which
specifies the root path name `'user'` and the root structure `'User''`. This root clause is important when
 providing `path` references within Motif queries.

#### Structure Clause
    structure User {
        0) flurryId : string key
        1) project : Project
        2) sessions : vector[Session]
        3) segments : vector[Segment]
        4) channels : vector[Channel]
        5) personas : vector[Persona]
        6) deviceModelId :  long
        7) deviceSubModelId :  long
        8) parameters : map[string, string]
    }

The body of the schema is in the form of an unordered list of structure definitions. Each structure has a 
name e.g. `'User'` and a set of `'relations'` that model the data contained within the structure. The relations
are numbered and named e.g. `'0) flurryId '` and have a operator and optional set of `'classifiers'` e.g. `'string'` 
and `'key'`.

##### Relation Numbering
Relation numbering should be in lexical order of field placement within the specification and  have no duplicates. If and when we have
schema `evolution` it will become critical to have these fields unchanging across versions and never
reused as they are deleted and added. Schema evolution is __NOT__ yet supported since Motif is not yet
used to define persistent storage.

##### Relation Types
Relations fall into the following categories.

###### Value Scalar Relation
    6) deviceModelId :  long 
A single primitive datatype

###### Value Vector Relation
    3) aVectorField : vector[boolean]
A vector of primitive datatypes.
       
###### Value Map Relation
    8) parameters : map[string, string]
An associative primitive datatype to primitive datatype map.
           
###### Reference Scalar Relation
    1) project : Project
A reference to a single structure.

###### Reference Vector Relation
    5) personas : vector[Persona]
A collection of references to a typed structure.
    
##### Data Types
    boolean
    byte
    short
    integer
    long
    double
    string
The primitive datatypes are chosen to provide a reasonable selection of options to handle most if not all 
requirements efficiently without gilding the lily.

##### Schema Classifiers
        0) flurryId : string key
        1) installTime : long ordinal
Relations in a structure can have zero or more `classifer` keywords added to the end. There are currently
two, `ordinal` and `key`. 

###### Ordinal Classifier
An `ordinal` classifier denotes that the annotated field is used to _sort_ any
collection that the containing structure places it in. This guarantees temporal or causal ordering
where appropriate.

###### Key Classifier
The `key` classifier denotes that the annotated field is `unique` within its containing vector. 
This guarantees the ability to uniquely identify any `instance` within a tree given a suitable path 
with appropriate key values e.g. `user[flurryId1].sessions[startTime1]` would give a unique session 
in user with `flurryId=flurryId1` and `startTime=startTime1`. __NOTE:__ This feature is not yet supported.

##### Schema Paths
    user.sessions.events.eventId

Since all Motif schemas describe an object-tree without cycles, the `root` clause and a `'.'` 
separated sequence of relation names navigations fully describe any `Path` from that root 
to any relation. These are used as a way to not only describe data access in a Motif filter,
but to reason through the context for the evaluation of that data within an object tree
traversal.