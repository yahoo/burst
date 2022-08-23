![Burst](../../../documentation/burst_small.png)


# `Burst Data Model Architecture`

Burst represents a significant architectural investment in
a custom behavioral-optimized approach to data modeling.

The most salient data concepts are:

1. A Burst behavioral analysis is applied to a`dataset` which
   is best conceptualized as an enormous number of
   individual complex-object-trees, each called an `Entity`, partitioned into
   one or more `Slices` that are distributed across some or all of the
   compute nodes (workers) within the Burst Cell (cluster).
2. Each of these complex-object-tree entities represents a single behavioral
   analysis target e.g. a person, a mobile or PC device, customer, client,
   manufactured-assembly, or medical patient.
3. This object-tree must contain one or more `time` or `causal ordered`
   `event-collections` that represent the `behavior` of this entity. The
   very basis of behavioral analysis is derived from and dependent on,
   the scanning of these sequences.
4. The entity object-tree is an acyclic, rooted,
   tree of objects representing a network of strongly modeled
   singular or plural relations between object(s)
   i.e. a `strongly-typed object-model`
5. The Burst data model support  subsystem is called `Brio`.
   Brio contains a complete type-system, a language-driven schema with
   parser/validator for
   authoring entity models, as well as subsystems for encoding, decoding
   data to and from a highly specialized binary format called `Blobs`.
6. Behavioral analysis execution against this data involves the `scan` or `depth-first-search`
   (**DFS**) of the entire object-tree including ordered iteration of
   the time/causal ordered sequences.
7. The `results` of each of these scans of each of these entities is then
   merged together to provide a final result.


## datasets

![](../../../image/burst_dataset.svg)

Behavioral analysis as Burst defines it is quite unique in that each dataset to be analyzed
must be modeled as a set of individual entities, where each entity is a complex object-tree,
that contains causal or time ordered collections of events along with a rich set of
unordered fields and collections of values and objects. There is _no ordering between
entities_ and _no direct relationships between entities_ and _no access paths between entities_
that can be explored or asserted.

## two phase analysis

![](../../../image/burst_two_phase_analysis.svg)

The basic premise is that the analysis consists of **two phases**:

1. a _depth-first-scan_ of each entity that explores the rich internal modeled world with
   very few limitations on the types and relationships. This produces a well-defined
   set of results that is in its own schema.
2. a _merge_ of all scan results across all entities. The semantics of this merger across
   entity results can take multiple forms. Burst has a default one we will describe later.

Its important to consider carefully if this restricted semantic is a supportive of
your application domain modeling needs.
Remember that an entity is something out there, physical or
otherwise, whose behavior you are trying to understand as it relates to the behavior
of some or all of the other entities.

## Brio type system

![](../../../image/burst_object_tree.svg)

The Brio type system supports the authoring of typed object-trees that contain
the structure and state associated with an `entity` in a dataset. The following
semantic rules apply:
1. The `object-tree` consists of one or more `typed-structure` instances
2. each `typed-structure` contains one or more `typed-relationship` instances
3. each `typed-relationship` is either `scalar` **(1:1)** or `vector` **(0:N)**
4. each `typed-relationship` is a `value-type`  or a `reference-type`
5. the  `reference-type` is a relationship to a `typed-structure`
6. the `value-type` is a relationship to a `primitive-value`
7. This means there are four relationship types:
    1. `value-scalar` -- a 1:1 relationship with a `primitive-value`
    2. `value-vector` -- a 0:1 relationship to (collection of) primitive values
    3. `reference-scalar` -- a 1:1 relationship with a structure
    4. `reference-vector` -- a 0:1 relationship to (collectionof ) structures
8. There is a specialized `value-vector` called a `value-map` that is a collection of
   `value-value` associations
9. there is always a `root-relationship` which is a `reference-scalar` pointing to a
   `typed-structure` which is the `root` of the tree.
10. there are no cycles (its a tree!)
11. there are a fixed set of `primitive-value`
    1. **boolean** - single byte logical value
    2. **short** - two byte fixed value
    3. **integer** - four byte fixed value
    4. **long** - eight byte fixed value
    5. **double** - eight byte floating value
    6. **string** -- all strings in Brio are contained in Dictionaries. Each object-tree has a
       dictionary. What this means is that
       a string value in a Brio object-tree is actually an index lookup into the object-tree
       dictionary.
12. all relationships can have a `null` value which means the reference or value is _unknown_.
13. within a `typed-structure`, a single `value-scalar` relationship
    can be annotated with the key word `ordinal` which means any collection containing that `typed-structure`
    will be sorted/ordered using the natural ordering of that `value-scalar`.
13. within a `typed-structure`, a single `value-scalar` relationship
    can be annotated with the key word `key` which means any collection containing that `typed-structure`
    will treat that as a `primary-key` within the collection

#### schema versions
Brio schemas can be versioned. (NEED MORE)

### schema specifications
Authoring of entity object-tree data models using the Burst type system is done
using two schema language specification files, a high level `Motif` _language_ schema,
and a low level `Brio` _encoding_ schema. Both of these need to be resources in the
classpath for the Cell nodes as well as data import systems that use
Burst standard libraries.

##### Motif schema
![](../../../image/burst_motif_schema.png)

The Motif schema is used by the EQL language as well as in the data import system
SampleStore. It is a pure object model schema definition.

##### Brio schema
![](../../../image/burst_motif_schema.png)

The Brio schema is used by the lower level data systems. It has

##### Brio and Motif compatibility

## merges

## blobs

## pressing
![](../../../image/burst_pressing.svg)

## data organization/partitioning
![](../../../documentation/image/burst_data_model.svg "")


### slices

### regions





