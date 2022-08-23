![Burst](../../documentation/burst_h_small.png "") 
--


# Sample Store View Properties
Imports are controlled via a set of properties stored in the associated metadata View object.
 
These come in three flavors:
1. __client-provided:__ The Burst client can optionally provide these to help control next load parameters
2. __system-suggested:__ The Burst system provides to help the client optimize the next load (updated each load)
3. __system-actual:__ the Burst system provides to help the client understand what happened in the last load

|  type | property | description |
|---|---|---|
|  client provided | `burst.view.next.dataset.size` | _Desired max size in bytes for dataset in next load. (default 100000000000 for 100GB)_  |
|  client provided | `burst.view.next.load.stale` | _The elapsed time in ms after `burst.view.last.load.at` before initiating a new load on next dataset (default 86400000 for 1 day)_  |
|  client provided | `burst.view.next.slice.count` | _Desired partitioning of dataset in next load (default 0 for no preference)_  |
|  client provided | `burst.view.next.sample.rate` | _Desired sampling for dataset in next load (default 1.0 for no sampling)_ |
|  client provided | `burst.view.next.item.max` | _Desired max item size (in bytes) for next load (default 10000000 for 10MB)_ |
|  system suggested | `burst.view.suggested.sample.rate` | _The system calculated sample rate recommended for next load. If `burst.view.next.dataset.size` does not change, using this value for `next-sample-rate` should almost always result in a valid load._|
|  system suggested | `burst.view.suggested.slice.count` | _The system calculated slice count recommended for next load_|
|  system actual | `burst.view.last.dataset.size` | _The actual byte size of the generated dataset (in bytes)_ |
|  system actual | `burst.view.last.load.at` | _The epoch time of the load time in ms of the last generated dataset_|
|  system actual | `burst.view.last.load.took` | _The actual load time in ms of the last generated dataset. This number can be used to help client understand generally how long new loads will take but be aware that system loads times are influenced greatly by external, potentially significant, dynamic factors_|
|  system actual | `burst.view.last.load.stale` | _The elapsed time in ms after `burst.view.last.load.at` before initiating a new load_|
|  system actual | `burst.view.last.slice.count` | _The actual slice count of the generated dataset_|
|  system actual | `burst.view.last.load.invalid` | _true if the `burst.view.next.dataset.size` and `burst.view.next.sample.rate` were not achievable_|
|  system actual | `burst.view.last.rejected.item.count` | _number of items rejected cause they exceeded `burst.view.next.item.max`_|
|  system actual | `burst.view.last.item.size` | _the actual average bytes/item for this dataset_|
|  system actual | `burst.view.last.item.variation` | _the actual variation factor for item sizes_|
|  system actual | `burst.view.earliest.load.at` | _The earliest epoch time in ms when a cold load would be triggered. This is a best guess by the system as it can not determine before hand, things like the dataset missing a slice in a loaded dataset due to node failure _|
|  system estimated | `burst.view.last.potential.item.count` | _the potential number of items extrapolated from the portion of bucket scanned_|


---
------ [HOME](../../burst-samplestore/readme.md) --------------------------------------------
