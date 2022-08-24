![Burst](../../../../../../../../doc/burst_small.png "")
![Fabric](../../../../../../../fabric_small.png "")

# Fabric Metadata

### Domains & Views
__Domains__ and __Views__ are the two major meta-data types associated with the
definition of datasets associated with a specific user dataset. Domains
are an abstraction of a source of data for a dataset, e.g. a specific mobile
application with a set of recorded user events of various types; or retail franchise locations in 
a particular geographic/political region with their sales, returns, and incoming and outgoing shipments. 

Views are a particular set of choices about what to include
in a specific dataset based on its parent Domain, e.g. 40 days of a particular
list of event types for the Domain's mobile application; or only franchise locations that did not exceed a specified
revenue target.

### Generations
A __Generation__ is a specific instance of a Domain/View that is a snapshot of the
data associated with a Domain, in a specific View, taken at a specific time. Each new
generation of a Domain/View combination is a new _version_ of the data in that Domain/View . 
You can think of it as a _snapshot_ of the Domain/View, and generally
you only want the most recent one. Each Generation of a given Domain/View is a unique dataset has
its own specific physically manifested data associated with it that can be made to exist, and may
already exist in the distributed data world of the Burst Cell. Generations are not permanent and
as new Generations are created, older Generations are allowed to, and will eventually, become stale
and disappear.

### Generic View Properties

These come in three flavors:
1. __client-provided:__ The Burst client can optionally provide these to help control next load parameters, any property
  not present in the view will revert to system default values.
2. __system-suggested:__ The Burst system provides to help the client optimize the next load (updated each load)
3. __system-actual:__ the Burst system provides to help the client understand what happened in the last load

|  type | property | data-type | description |
|---|---|---|---|
| burst.view.evict.ttl.ms | client-provided | `long` | The number of milliseconds after the last access the worker cache waits before evicting a view (clearing from memory). |
| burst.view.flush.ttl.ms | client-provided | `long` | The number of milliseconds after the last access the worker cache waits before flushing a view (clearing from disk). |
| burst.view.erase.ttl.ms | client-provided | `long` | The number of milliseconds after the last access the worker cache waits before erasing a view (completely forgetting). |
| burst.view.next.dataset.size.max | client-provided | `long` | The client requested maxmium amount of data that should be loaded on the next load. This value is opaque to Burst and is passed to the store responsible for loading data. |
| burst.view.next.load.stale | client-provided | `long` | The number of milliseconds after which the current generation should be marked as stale and reloaded. |
| burst.view.next.sample.rate | client-provided | `double` | The desired sample rate to be used on the next load. Values outside the range `0 < rate <= 1` will likely be ignored |
| burst.view.suggested.sample.rate | system-suggested | `double` | The suggested sample ratio, computed by dividing the count of items received by the number of potential items in the dataset |
| burst.view.earliest.load.at | system-actual | `long` | The epoch timestamp when the most recent generation was first loaded. However, a generation could potentially be reloaded after this date |
| burst.view.last.dataset.size | system-actual | `long` | The actual number of bytes that were loaded from the fabric store during the most recent load |
| burst.view.last.dataset.size.max | system-actual | `long` | The limit for the most recent load, as determined by the cell and the store |
| burst.view.last.item.size | system-actual | `long` | The number of bytes loaded during the last generation |
| burst.view.last.item.variation | system-actual | `double` | ??? |
| burst.view.last.load.at | system-actual | `long` | The epoch timestamp when this generation was loaded from the fabric store (in milliseconds) |
| burst.view.last.load.invalid | system-actual | `boolean` | If there was an error condition detected during the most recent load |
| burst.view.last.load.stale | system-actual | `long` | The epoch timestamp after which the data will be considered stale and reloaded (in milliseconds). This is computed by adding `burst.view.next.load.stale` to the time of the most recent load |
| burst.view.last.load.took | system-actual | `long` | The duration of the slowest slice of the most recent cold load (in milliseconds) |
| burst.view.last.expected.item.count | system-actual | `long` | The number of items that the store expected this generation to contain |
| burst.view.last.potential.item.count | system-actual | `long` | The potential number of items the generation would hold without sampling or size constraints |
| burst.view.last.rejected.item.count | system-actual | `long` | The number of items that were discarded by the remote store during the load process |
| burst.view.last.slice.count | system-actual | `long` | The number of slices during the most recent load |


---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
