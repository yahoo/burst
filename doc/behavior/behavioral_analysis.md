![Burst](../burst.png "")

# `Behavioral Analysis`

---
###### _"Burst is a unique language first approach providing clear, deep, and fast insight into very large, very noisy, behavioral datasets"_

---

___Burst___ is a **_behavioral_** database. It was designed from the ground
up to support asking _behavioral_ questions about _behavioral_ 
data. In the vast ecosystem of various types of 
systems that allow you to ask questions
about data, **Burst** carves out a fairly specialized domain.
As you get to know **Burst** 
we want to be very clear about the nature and implications of that
specialization, so you can determine the Burst data model's match
to how you currently model (or can transform) your data, and how **Burst**'s
analysis capabilities can help satisfy your curiosity about it.

### `What is Behavioral analysis?`
[Behavioral Analysis](#Behavioral_Analysis)  as **Burst** defines it,
is the study of [Behavioral Data](#Behavioral_Data) in order to 
extract, measure, and organize strong and clear empirical signals which lead
to greater knowledge and insight about that data. Not all applications
have or can easily change
their data to be organized in what we think of as a behavioral 
model, and not all
applications care to ask the types of questions we would consider
behavioral. For example this is meaningfully different from  a relational
model and standard relational queries.

### `Behavioral Data`
![](behavioral_data.svg "")
Behavioral data as **Burst** defines it, is a large set of complex
`entities` each of which exhibit a sequence of `behaviors` 
generally modeled as `events` which unfold causally or temporally
i.e. sorted in _ascending_ (or _descending_) time or sequence order. 

This translates to the following rough truths about behavioral data:

* there are a very large number of `entities`
* each `entity` has its own rich data model 
* the entity model is in the form of a `tree` 
* there are no direct entity-entity relationships - each object tree is internally consistent 
* each `entity` contains a large number of causal/temporal `event` sequences
  that represent its `behavior`
* these sequence collections tend to be of a high-cardinality (large)
* each `event` has its own rich data model
* there is value in understanding the individual and aggregate behavior
of these entities and their event sequences
  
In order for **Burst** to do its magic, it has to be able to have the 
data it is analyzing organized
based more or less along the above guidelines. Note that often data can be _transformed_ into
this sort of model as it is imported from the data source location. More on 
this later...

### `Behavioral Analysis`
Behavioral analysis is asking questions about behavioral data.
The questions generally are some combination of the following aspects:

| aspect  |  a.k.a. | e.g.  |
|---|---|---|
|  `measuring` |  aggregation |   _count how many ( uniques, min, max, etc)_  |
|  `grouping` |  dimensioning | _collate by values, time buckets, splits, or ranges_ | 
|  `filtering` |  selection |  _where age > 4 and time between now and a month ago_ | 
|  `temporal` |  causality | _this was followed by that then by either of two other things depending on some other stuff_ | 
 

## `what does a behavioral question look like?`
All this leading to questions that look like...

---
**_how many things where doing what between this time 
and that time grouped by some set of characteristics?..._**
---

or put another way...

---

**_behavioral analysis is looking across a vast sea of entities behaving in some way and 
filtering that list down to one or more specific 'cohorts of interest' and then 
measuring some salient parameters about them  organized into some insightful set of 
categories all of which tells you something 
valuable about the behavior of those cohort(s) and 
how they compares to other cohort(s) or possibly 
the entire entity superset._** 

---

That being said, there are in fact many other types of questions that _can_ be asked and
in fact **Burst** can be quite effective at many types of questions that don't look like the above.
Also **Burst** is quite extensible so we would never want to limit anyone's thinking about broadening
its repertoire.  But generally speaking the **Burst** sweet spot is
questions that follow these general guidelines.

## `is data sampled?`
Burst will sample data automatically if during import the size exceeds a 
specified amount. There are a few things that Burst does to reduce this problem:
1. Supports import of extremely large datasets via a highly scaleable 
multinode architecture with parallel data import network protocols.
1. Supports a language based filtering on import model that allows for creation
of views that are subsets of the entire authorative dataset.
   
Other than that there is no magic. While Burst compresses here and there to
optimize resource utilization, it is lossless other than sampling and so there 
is a price to pay. 

## `How interactive and/or ad-hoc is this?`

Burst was designed to support both ad-hoc interactive sessions as well as 
batch mode (reporting) style workloads
in the background or foreground. All of this at high request concurrency
and high transaction rates. It can also load large numbers
of datasets at once and manage moving them from disk to memory via
its cache _very_ efficiently.
That being said its important to digest a 
few issues:
1. There is generally a `cost per data load`. Loading data once and then running
    multiple behavior analyses against that data load are one of Burst's sweet spots.
   This also fits in with the virtuous cycle of question/answer/better question
   that is so important in drill-down analytics. 
1. There is a cost per unique parameterized analysis.
   The key word here is parameterized. If you design your analysis to take
   parameters then the cost is only paid once. Each subsequent invocation
   with different parameters does not pay this cost. This includes an additional
   bonus of increased JIT optimizations because we cache the bytecode on the
   workers. This is a significant topic that is more greatly clarified elsewhere.
1. Burst supports parallel analysis i.e. you can submit an analysis that is actually
more than one to be run in parallel against the same domain-view-generation. These
   batch mode analyses are more efficient than submitting one at a time. Also
   see parameterization above...
   
**Bottom line** is that Burst is an `on-demand data import/cache style system`. 
We support fast massive parallel data loading which ameliorates
this somewhat, but still its something to consider carefully.


---
------ [HOME](../../readme.md) --------------------------------------------
