![Burst](../../../../../../../../../../documentation/burst_h_small.png "")
![Burst](../../../../../../../../doc/fabric_small.png "")

# Fabric Configuration

## Worker Caches
| system property                        | default             | description                                               |
|----------------------------------------|---------------------|-----------------------------------------------------------|
| burst.fabric.cache.boot.flush          | true                | flush all slices on boot                                  |
| burst.fabric.cache.tend.period.minutes | 5 minutes           | how often to tend the cache                               |
| burst.fabric.cache.stale.hours         | 24 hours            | XXX                                                       |
| burst.fabric.cache.memory.high.percent | 40                  | high water mark memory usage percentage                   |
| burst.fabric.cache.memory.low.percent  | 25                  | low water mark memory usage percentage                    |
| burst.fabric.cache.disk.high.percent   | 60                  | high water mark disk usage percentage                     |
| burst.fabric.cache.disk.low.percent    | 50                  | low water mark disk usage percentage                      |
| burst.fabric.cache.fault.heal.duration | 10 seconds          | how long to prevent retrying a load from the remote store |
| burst.fabric.cache.regions             | worker thread count | XXX                                                       |
| burst.fabric.cache.spindles            | single tmp folder   | a ';' separated list of folders on separate drives        |
| burst.fabric.cache.impellers           | 8                   | how many impeller threads per spindle                     |
| burst.fabric.cache.parcel.enable       | false               | enable parcel pipeline mode                               |
| burst.fabric.wave.concurrency          | 12                  | maximum number of concurrent waves                        |


---
------ [HOME](../../../../../../../../../readme.md) --------------------------------------------
