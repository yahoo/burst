![Burst](../../../../delete/open-source/doc/burst_h_small.png "") 



    Burst is a generalized database system focused on analyzing the 
    behaviors of entities that produce ordered sequences of events.  
    It excels at measuring, organizing, and pattern matching trends 
    and causes for behaviors --- even in very large historical records.  
    Burst finds these behaviors even though they can span time and be 
    buried in the noise of insignificant actions.  While traditional 
    database solutions have been used to analyze behaviors, their original 
    focus on relationships instead of causality means they fail in 
    performance when used at scale. Burst solves the behavior analysis 
    problem through innovative architecture and design.

# `The what, why, and how of Burst`

---
## Introduction
With any data analysis system in the vast mix of industry proprietary and open-source 
alternatives, there are basically three questions one needs to ask.  `What` is it, `why` is 
it valuable, and `how` does it work? Here we will address those questions as well as 
endeavour to clarify where Burst adds unique and differentiated value and how that 
value is provided.

## What is Burst?
First off, Burst fits solidly into a specialized category called `behavioral analysis`.  
We use the term behavioral very carefully in order to distinguish Burst from the much 
more common relational analysis category. Burst allows you to ask behavioral questions 
about behavioral data, whereas relational analysis systems allow you to ask relational 
questions about relational data.  

#### Relational analysis
`Relational analysis` systems understand data as one or more tables each with zero or more 
rows of one or more columns of values. The ubiquitous SQL language API allows you to ask 
questions that relate the rows in one table to the rows in the same or a different table(s) 
using operators such as filter, aggregate, project, group, and join. Often the tables are 
designed to efficiently contain a very large number of rows and thus support fast complex 
questions.  Mostly these systems are transactional & persistent data storage systems i.e. 
these questions can write as well as read the data and that data is permanently available, 
resilient to failure, and provide simple consistent semantics in the face of concurrent 
access. Because questions are asked directly to this reliable store, the answers are 100% 
‘current’ or ‘up to date’.  Examples of relational systems are MySql, Postgres, Oracle, 
Postgres, and many many others.

#### Behavioral Analysis
`Behavioral analysis` systems however, understand data as a set of entities that emit complex 
causal or time-ordered events that represent their historical behavior. Generally an 
altered SQL language API is provided that allows you to ask questions about all or a 
subset of these entities and their historical events using operations such as filter, 
aggregate, group, and sequence-match (temporalize). The resulting answer is a table or 
set of rows that capture an interesting and valuable pattern or signal in a large, 
complex, often noisy dataset.  The goal is to have this answer clearly illuminate 
essential and otherwise obscured normative/outlier characteristics exhibited by some 
interesting cohort within the greater community of entities. These systems are generally 
operating on ephemeral ETL snapshots imported as a time window from a stream or from a 
remote database or storage system.  There are not very many behavioral analysis systems 
available out there because most aren’t generalized frameworks and many are simply bespoke 
code implemented on top of key-value stores or databases. Examples of generalized 
behavioral systems are Burst and Druid.

### Examples of behavioral data:
* user interaction with a software application
* service consumer interactions with a service provider
* SKU interactions with a sourcing/manufacturing/logistics process

### Examples of behavioral analysis:
* how many users in the last week performed a sequence of screen interactions that led to a purchase grouped by gender, region, time of day, and purchase value
* how many visits with what sort of outcome did patients require to complete a health care procedure grouped by quarter, hospital, procedure, and primary doctor.
* how many cars with a specific list of processing steps were processed last year grouped by trim line, plant, and number of defects.

## Why is Burst?
It is possible to store behavioral data in a relational system and ask behavioral questions
using carefully crafted relational queries. It is also possible to put your data in a 
key-value store e.g. Hadoop or HBase, and use carefully crafted map-reduce based 
processing models e.g. Hadoop or Spark. But for good reason these awkward cross 
fit architectures are problematic in terms of performance, ease of use, flexibility, 
and/or implementation complexity when trying to deliver a general purpose ad-hoc 
behavioral solution. Real world experience shows that  large behavioral datasets, 
tight latency constraints, and full featured behavioral questions dictate highly 
optimized and quite specialized architectural approaches.

Behavioral calculus requires stunning amounts of highly repetitive low level data 
accesses and calculations such as counting and grouping. The math involved is for the
most part trivial but the data structures and algorithms must be flawless in how they 
scale in terms of both CPU and memory demands. Great attention must be paid to how the 
data is binary encoded, distributed across nodes and loaded into modern multicore memory 
architectures. The high-level language driven analytic queries must be processed into 
perfectly distilled inner loop algorithms that are sequences of low-level 
hardware-sympathetic native instruction sequences that drive all the cores on a given 
node to near 100% utilization. The query requests must be scattered across hundreds or 
even thousands of processing nodes, all data processed and the results gathered fast 
enough to sustain the end user in a friction free ‘virtuous’ interactive 
question/answer/better-question cycle. These challenges are a make or break 
proposition for even the simplest queries given the enormous data sizes involved. 
Where in some domains a query simply is less fast that desired, in this domain a 
poorly executed query can literally become unstable and never return.

## How is Burst
The way that Burst addresses this performance challenge is by first providing a 
highly scalable distributed worker data processing topology. Burst scales across an 
almost unlimited quantity of worker nodes each with the vast number of cores that 
characterizes modern server hardware, almost without limit.

Secondly, Burst provides a proprietary/extended SQL-like language pipeline transforming 
human friendly high level questions into tight code-generated, hardware sympathetic distributed/parallel query processing plans. Along with the basics of counting, filtering, and grouping/dimensioning, it also provides a toolkit of specialized causal/temporal pattern matching semantics.  Burst allows you to ask almost unlimited complexity behavioral questions about extremely large numbers of complex entities in interactive time.

Finally all of this sits on top of a bespoke data model based on a specialized 
schema-driven binary-data entity encoding approach. Each entity in a behavioral 
dataset is a carefully designed object-tree. The Burst processing model depends on 
extremely fast efficient scans of these entity models where each scan is a 
depth-first-search of the entity tree including the time ordered event histories 
that capture the semantics of its behavior. These scans must be hyper efficient in 
both time and space and always parallelize effectively across many threads. The 
data model is designed to be stored (cached) efficiently on disk and page rapidly in 
and out of memory on  linux file systems. Both SSD and striped magnetic drive storage 
is supported. The data is highly compressible and network friendly. Burst includes a 
refined parallel network data transfer protocol for very fast imports of data views 
from highly parallel distributed data storage systems such as Hadoop/HBase.

Common Burst deployments would contain hundreds of worker nodes each with dozens of 
cores all running near peak speed and efficiency. All of these components have gone 
through multiple generations of ruthless elimination of excess CPU cycles and memory 
churn.  It is simply impractical to repurpose a relational system or 
key-value/map-reduce system to match this sort of uncompromising singular focus. 
If you have the normal enormous amounts of behavioral data and you want to ask 
interesting behavioral questions with answers in interactive time then a system 
like Burst is likely to be your best if not only option.

# Summary
`Burst` represents an uncompromising architecture in the  behavioral analysis system 
solution space. It applies a kitchen sink of best practices to the many and 
significant challenges posed by very large datasets and industry demands for an 
ad-hoc, easy to use, expressive, fast, and flexible analysis framework.  
