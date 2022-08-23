![Burst](../documentation/burst_h_small.png "")
--

![](./doc/motif.png "")

```'recurring salient thematic element...'```

___Motif___ is the support library for a filtering language called Motif, meant to provide a simple
universal common filter-query semantic across various external and internal subsystems.  For instance
the default SampleStore uses Motif to describe how datasets are sampled, time-boxed, and content-filtered. Other systems
that want to use this have only to create an execution engine for their Sample Source to match.

This module consists of the Motif language definition (as [Antlr4](http://www.antlr.org/) grammars) along with
a parser for that language, a runtime support library (as a _fat_ jar) for internal and external
Motif users as well as (this) documentation. The deployed artifact is a single __shaded uber jar__
designed to support authoring, validation, and error-checking of Motif filters as well as a pre processor for
various and sundry back ends performing direct execution of Motif queries or translation of Motif
semantics to external query systems.

### Concepts

* __Motif Schema:__ Motif makes use of a simple Motif schema IDL which fully describes a nested recursive object-tree model.

* __Data Model:__ Motif is used to manipulate datasets in a data-model `D`, defined to be of a set of one or more
tuples `T<K, V>` where `K` is a unique key, and `V` is a nested recursive  object-tree conforming to a
provided Motif schema `S`.

* __Language:__ Motif provides a simple and consistent combinatorial predicate calculus language that can be used to
specify a filter query `Qf` that specifies the inclusion or exclusion of tree elements across all tuples
in a data-model `D`.

* __Reduction:__ Motif is designed to support the transformation of an input dataset in schema `S` to a reduction of
that dataset also in schema `S`.  This reduction takes two forms; _pruning_ and _subsetting_.

* __Pruning:__ The pruning reduction is where a tuple `T1<K1, V1>` in the input dataset is transformed to another
tuple  `T2<K2, V2>` in the output dataset where the object-tree `V2` is object-tree `V1` with zero or more
object-tree elements removed. Note that this operation is _schema-invariant_.

* __Subsetting:__ The subsetting reduction is where the specified pruning of `T1` would result in the removal of the root
of the object tree in `T2`. In this case the entire tuple `T1` is removed from the dataset.

* __Optimized Subsetting:__ Though conceptually reduction leads to subsetting via pruning, it is a possible
optimization to short circuit pruning and go directly to subsetting based on inherent knowledge of the
underlying execution model. e.g. a time based pruning at the root of the object tree may be optimized
to be reduction in the number of tuples read from the disk or deserialized.

### Documentation
* [View Examples](doc/view-examples.md)
* [Language Specification](doc/languages.md)
* [Java Runtime Support](doc/runtime.md) (including schema discovery)
* [JSON Runtime Support](doc/json.md)



---
------ [HOME](../readme.md) --------------------------------------------
