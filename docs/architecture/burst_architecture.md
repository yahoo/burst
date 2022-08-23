![Burst](../../documentation/burst_small.png)

# The Burst Architecture


![](../../image/burst_runtime.svg)

The goal of the `Burst` architecture is to take a client request
with an expressive user-friendly
language based specification describing a desired behavioral analysis 
along with a specification for an appropriate client dataset, and
to extract
the specified analysis from the specified dataset as
fast and efficiently as possible, delivering
results in a useful form back to the client with low latency.

This goal has translated to a system architecture design focused on the distribution of language
driven queries from a master node out to multiple worker nodes where rapid parallel scans
of carefully optimized binary data are performed, results gathered,
merged with other results, and passed back to the master node for a final 
merge. 

## architectural themes
This architecture was made possible by focusing on 
three overarching themes that you will see directly or indirectly
referenced throughout the `Burst` architecture. Efficient single pass scans, 
reduction of long tail processing returns, and generally high levels of 
mechanical sympathy i.e. making modern hardware perform at peak levels.

#### hyper efficient single pass scans
Behavioral analysis is ultimately just a lot of looking at a lot of data records
that each represent some sort of entity whose behavior is being analyzed,
and performing some sort of calculus on the contained value fields. This calculus
while mostly fairly simple needs to be extremely fast and efficient because
of the enormous plurality of those data records and the large collections of field values
they contain. For instance if the entity is a mobile application, each data record
contains all the events recorded by that mobile application over a potentially very
long period of time. Each of those events can have a large number of data fields
and collections of records. Any significant slowdowns anywhere in that scan
can turn a quick analysis into one that takes forever or even worse becomes
unstable and causes system breakdowns. More on this later.

#### distributed partitioning of data/processing and long tail reduction
Assuming we have efficient entity record scanning, the next challenge is to
be able to partition/distribute all this data and their scans out to a 
plurality of nodes and their cores,
where these scans can be executed. We want to be able to scale up to
more nodes and cores if we need to with almost no limitation.
The challenge that goes along with that ideal is that you are creating
a large loosely connected tree of distributed processing 
points. This somewhat complex multi-part
distributed processing operation _will take as long as the slowest part_. When
there are tens of thousands of parts (or more!) you need to be very careful to divide up
the work symmetrically and make sure every single processing point operates at a reasonably 
predictable and consistent speed. This is not a small problem.

#### mechanical sympathy
In order to get Burst to meet this challenge in a cost effective way, 
we need to consistently push the hardware it is deployed on
to a very high level of consistent speed and efficiency. This is a complex
multifaceted
topic  but includes great focus on Java virtual machine behavior, 
multi-processor/core CPU memory quirks,
and of course specialized data structures throughout. 

#### dataset size, signal-seeking, noise, and sampling error
One very large challenge with Behavioral Analysis systems is that for a given
analysis effort, often the associated data as it
is stored in its origin authoritative store is very large. There may be no practical
way to import and/or scan the entire amount so workarounds need to be created.

Generally this takes the form of:
1. _sampling the entities_ i.e. importing only a subset
of the entities and analysing the normalized results for that subset as being 
representative
2. _filtering the within the entities_ i.e not capturing all the contained 
collections or fields.

Which workaround is best is not only dependent on the specific data involved but also
on the type of analysis desired. 

The way that Burst handles this problem is multifold:

1. efficient/effective handling of very large datasets per deployed worker node
2. ability to scale to a large number of deployed worker nodes
3. ability to create views which specify time-windows on data import
4. ability to create views which specify entity level sampling on data import
5. ability to create views which filter within the contents of a given
    entity data record on data import

These techniques are not a panacea. Analysing very large datasets often requires 
hard choices that sometimes lead to unwelcome compromises. 
Burst was designed to support implementing those choices and to reduce the 
compromise. 

Points to consider:
1. There are some analytic semantics
    that return fundamentally incorrect results when run over sampled data. 
2. There are some queries that
    can get 100% of the desired signal over a very small-time window or with only a fraction
    of the entity data record contents.
3. It should be noted that this is a major topic and needs to be managed 
    in an application specific way especially as concerns the exact 
    nature of the data import mechanism in use. 

#### fast on demand data import
Burst does not store data authoritatively. It relies on the Cell master and worker nodes
to have live connection to
a external store for on demand data import and caching. When an analysis query is run,
the specification for the data it is to be run on is partitioned and
sent out to the worker nodes along with the analysis specification. 
The worker nodes then are responsible for loading
the needed partitioned slices into their local cache.
Burst stores data in its cache only as a performance enhancement.  If a worker node is lost,
the worker node replacing it simply loads the same slice and continues on.
The performance
and usability of this import model supports parallel partitioned data loads
and thus faster loading of on demand data from large remote stores. This is especially
well matched with big data storage systems such as HBASE/HDFS where the Burst worker node
are designed to support such things as direct connections to HBASE region servers
or HDFS data nodes,
thus scaling a single data load
up to multiple parallel/concurrent worker to worker data streams. This is quite effective
and is the way that Yahoo's Flurry Analytics product 
does fast and efficient loads of mobile application data into Burst.

## architectural areas
The total Burst working theory of operation can be categorized and distilled down
to a specific list of architectural concepts/approaches. Many of these areas are
simply best practices for eliciting high performance out of
linux servers, distributed architectures, and modern CPU/memory systems.
However, individually and collectively, each of these
is critical in its own way to the fast, consistent,
cost-effective, low-latency processing of the enormous data-sets and rich
analytics that `Burst` was designed to provide.

1. [data model](concepts/burst_data_model.md)
4. [data management](concepts/burst_data_management.md)
2. [single-pass scan](concepts/burst_singlepass_scan.md)
3. [the language pipeline](concepts/burst_language_pipeline.md)
1. [data structures](concepts/burst_data_structures.md)
1. [runtime topology](concepts/burst_runtime_topology.md)
