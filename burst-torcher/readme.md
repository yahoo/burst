![Burst](../doc/burst_small.png "")
--

![](./doc/torcher.png "")


___Torcher___ is a testing and performance service for stressing the Burst execution engine



# Running Torcher

Torcher is a controlled from a tab in the master node administration web page (port 37030). From this tab, any user
can start and stop a torcher run,  monitor an in-process run, and download statistics about current or past runs.
Only one torcher run is allowed to run in a cell at a time.

## Controlling Torcher

Torcher is controlled using a JSON document specifying various aspects of the torcher run.  The torcher source JSON
for a run is entered into the Admin source text box by either using the `LOAD` button to load the file using the browser
or entering by hand.  The source window allows subsequent editing of the source for later runs.

Once the torcher JSON is entered,  a torcher run is started using the `START` button.  Torcher will parse and process
the JSON document.  Once it is finished it will begin the run.

When torcher is runing the `START` button is changed to a `STOP` button for stopping the run manually.

## The Torcher JSON

The torcher JSON is divided into three sections:

1  The top level controls
2  Batches
3  Domains

### Top Level
The top level section has global controls for the torcher run:

* `stopOnFail` is a boolean field controlling if a torcher run will stop immediatly upon seeing an error.  The default
is `false` meaning torcher will record the error but attempt continue the run.  Setting this to true is usefull if
torcher trips a complex error that might be hard to locate in the logs due to subsequent queries and logging.

* `schemaName` is the schema torcher will use for all queries and viewd. The default is `unity`

* `parallelism` controls how many simultaneous clients will drive the torcher work load.  Any rate specifications will
be divided equally among the clients so as to achieve the given aggregate rate.  The default is `1`

* `duration` controls how long torcher will drive work.  Duration is specifiefd using joda duration syntax:  for example, `1 day`, `3 hours`, etc.
The special string `once` tells torcher to run through the list of views exactly once.  The default is `once`.  If a fixed time
duration is specified then torcher will run until it expires:  it will stop even if there are more domains left in its list
and it will go through the list as many times as it needs.

* `queryRate` controls how fast the list of queries will be executed for each domain in the run list.  The value is a double
specifying queries per second.  The value specifies an aggregate rate allocated amongst all the concurrent clients,
A value of `0.0` will instruct torcher to run the queries as fast as it can.  The default
is `0.0`.  The query rate cannot be so slow that the queries in the list cannot all be executed before the next load must
occur in order to maintain a specific load rate.

* `loadRate` controls how fast a new domain view will be loaded by torcher. The value is a double
specifying loads per second.  The value specifies an aggregate rate allocated amongst all the concurrent clients,
A value of `0.0` will instruct torcher to run the loads as fast as it can.  The default
is `0.0`. When running as fast as possible,  torcher will run through all the queries in the query list before starting
the next load.

* `timeout` controls how long torcher will wait for a query to complete.  It is an integer value specifying milliseconds.
The default is 10 minutes.

* `storeType` controls whether to use existing views for domains or create temporary torcher views.   If the `storeType`
field is *not* present, then torher will create sample source views for each domain it loads in a run.  Only one sample
store view is created for each domain in the torcher run list.  If a store type *is* specified then torcher will look
for the first existing view in the catalog with that type associated with each domain in its run list.   Some valid
store types are `canned`, `fuse`, `sample`.  So what is the difference between not specifying a `storeType` and specifying
a type of `sample`?  In the former, torcher will always create a special temporary sample view for each domain (even if
one exists already),  while the latter will look for the first existing sample store view for each domain (complaining
if it can't find one)


### Defaults
The top level section can also contain default fields that are applied to any batches or domains that don't specify the
fields specificially.  All sections can specify defaults.  Defaults are scoped by section so that a default for a inner
section overrides the parents defaults.

The default fields are:

* `flush` is a boolean which controls whether torcher should explicitly flush a view from the cache once it is done
loading and running the queries.  Turning flush on is not how things normally work in production so it is not
recommended mode.  The default is false.

* `motif` specifies the default view created by torcher when no store type and temporary sample views will be created.
The default view is

	VIEW template {
	INCLUDE user
	INCLUDE user.sessions.events where false
	}

* `queries` specifies the queries to run after a load.  The field is a list of
strings.  The queries can be any of the burst supported dialects:  SILQ, GIST,
HYDRA, or EQL.  The default query list contains the one entry:

	SILQ("unity", 1, 0)
	AGGREGATE (
	"Users" as count(user);
	"Sessions" as count(user.sessions);
	"Events" as count(user.sessions.events);
	"count" as sum(count(user.sessions.events) + count(user.sessions) + 1);
	)

### Batches
The next section of a torcher JSON document is the `batches` array.  Each `batch` object has the following
fields:

* `domainTag` tells torcher to search for domains tagged with the given string.  There is only one domain tag in
a batch so if you want to have a disjunction of tags then each tag must be in a separate batch.  More complex tag conditionals
are not supported

* `domains` is an array of domain sections.

A batch can override the default fields from the parents by including one or more `flush`, `motif`, or `queries` fields.

### Domains

The next section is the domain section.  A domain is an entry in the `batches` array of the batch object.  The fields
in a donain is *one* of the following:

* `pk` the numeric primary key of a domain in the catalog
* `moniker` the moniker of the domain in the catalog
* `projectId` the flurry project id of the domain in the catalog

And as with other sections a domain object can have one or more of the default fields to change the defaults.


## Using Torcher For Test Scenarios

* **Load test a new release of Burst or Beast to smoke out errors** -- Have torcher select 10
projects and run set of queries from a parameter file.
* **Load all active projects in specified amount of time** -- Tag the top
100 views and run torcher at the targetperformance rate
* **Support a target QPS** -- Create a file of representitive queries and run torcher with a specific rate
of query submission.
* **Support a target number of users signing on to the system** --

## Examples

A sample JSON document for running once through torcher of all domains tagged `testing` using temporary sample
store views is:

	{
	"duration": "once",
	"loadRate": 0.015,
	"queries": [
		"SILQ(\"quo\", 1, 0)\n AGGREGATE ( \n \"Users\" as count(user); \n \"Sessions\" as count(user.sessions);\n \"Events\" as count(user.sessions.events);\n)",
		"SILQ(\"quo\", 1, 0)\n AGGREGATE (\n\"Count\" as count(user);\n)\n DIMENSION (\n \"Month\" as MONTHOFYEAR(MONTH(user.sessions.startTime));\n \"Year\" as YEAR(user.sessions.startTime))"
	],
	"batches": [
		{
			"domainTag": "testing"
		}
	]}

This example has changed the default list of queries that each domain load will run and specified a load rate of 0.015
loads per second.

The next example specifies a number of domians by project id:

	{
	"flush": "N",
	"parallelism": 5,
	"duration": "once",
	"loadRate": 0.5,
	"batches": [
		{
			"queries": [
				"SILQ(\"unity\", 1, 0)\n AGGREGATE ( \n \"Users\" as count(user); \n \"Sessions\" as count(user.sessions);\n \"Events\" as count(user.sessions.events);\n)"
			],
			"motif": "VIEW pleasework { INCLUDE user WHERE (count(user.sessions) WHERE user.sessions.startTime > NOW-DAYS(365)) > 0; INCLUDE user.sessions WHERE user.sessions.startTime > NOW-DAYS(365); include user.sessions.events where false;}",
			"domains": [
				{ "projectId": 3214 },
				{ "projectId": 3236 },
				{ "projectId": 3402 },
				{ "projectId": 3403 },
				{ "projectId": 5005 },
				{ "projectId": 5575 },
				{ "projectId": 9248 }
			]
		}
	]}

The example has 5 parallel clients and a load rate of 0.5 loads per second.

## Load and StressTesting

Torcher is used to simulate the burst system at or above production levels.  Currently, flurry hits burst at about
0.015 Loads per seconds or about 1800 projects loaded over the course of 24 hours.  We usually get the list of active projects
ids for the last 4 days and use that in a torcher domain list.  The most recent load test JSON is [here](doc/flurry/recent.json)

Here is the current status of Sample store load testing on a 75 node cell:

| LPS | Parallelsm | Number | Duration | Result |
|:----|:-----------|:-------|:---------|:--------|
| 0.02 | 3 | 5 hours | | Some hm7 timeout errors |
| 0.02 | 4 | 5 hours | | no failures! |
| 0.075 | 4 | 5 hours | 687 | 50 host failures |




# What is Torcher Trying to Accomplish?

## Use cases
There a number of testing and performance use cases we want torcher to help us with:

1. Test error free repeated dataset load and evict cycles of fuse or sample datasets.
2. Test error free concurrent loads of both fuse and sample store datasets.
3. Test system capacity for simultanious loads of fuse datasets, sample datasets or both.
4. Test for error free, result correct execution of a set of test queries
5. Test how quickly we can load 100 sample views from the beast to fabric data cache.

We use torcher as a verification step for any release to production.  The goal is that torcher will simulate loads and
and concurrency rates well above what a production system will usually see.

## Functionality
Noticing that the functionality needed for each of the scenarios is similar, we use a single execution flow and only
alter the amount of work done at each step to talior it to the scenario.

The execution flow steps are:
1. Flush the dataset if it is already in the fabric data cache.
2. Load the dataset
3. Run a collection of queries
4. Flush the dataset.

#### Controls
There are some properties of the steps that we would like to vary according to testing
need:

* We want to control how many individual flows to perform.
* We want to control the number of concurrent flows.
* We want to control the rate of flows over time.
* We want to control the number of queries run at step 3.
* We want to control the rate at which queries are sent

#### Reporting
The torcher tools should provide ongoing and summary reporting and in a format that
can be digested by both the human and automation tools.

##### summary
After the torcher run completes the follow summary statistics will be displayed:
* total counts
* loads
* queries
*unload
* total time
* load
* query
* unload
* average time
* load
* query
* unload
* Rate
* load
* query
* unload
* Total errors
* load
* query

##### Selecting Datasets and Queries

Torcher needs to run over a specified set of views and use a set of general queries when running.
There are two ways of supplying this data:

* A query tag parameter tells Torcher to use all queries in the catalog with that
tag during the query step.
* A query file name paramter tells Torcher to read all queries out of the file and
use them in the query step.
* A project tag parameter and parameter telling torcher to iterate over any similarly tagged domains and
use the latest fuse dataset.
* A project tag parameter and a template parameter tells torcher to iterate over similarly tagged domains,
create a temporary view using the template, run the execution flow and then remove the temporary view.
* A project file name parameter tells torcher to read a file with rows of domain ids and view sources
and similar to the project tag, iterate over each domain, createing a temporary view and doing
the execution flow.

##### progress
Optionally torcher should periodically display interum reports of the current summary stastics.
Also it should report the following events as they occur:

* load error
* query error
---
------ [HOME](../readme.md) --------------------------------------------
