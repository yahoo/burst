![Burst](../../doc/burst_small.png "")

# Burst Samplestore Api

## Thrift API and Fabric Data Model
Along with `Finagle Thrift` client/server support in this module, there is a
[Fabric](../../burst-fabric/readme.md) based data model, with implicit translations to and from the
Thrift data model defined in [BurstSampleStore.thrift](src/main/thrift/service.thrift)

## Configuration
|  system property |  default |  description |
|---|---|---|
|  burst.samplestore.name |  "samplestore" |  user friendly name of application  |
|  burst.samplestore.api.host |  getPublicHostAddress |  interface to bind Thrift API  |
|  burst.samplestore.api.port |  37020 |  port to bind Thrift API  |
|  burst.samplestore.server.connect.life.ms |  infinite |  Thrift API server connection lifetime (in ms)  |
|  burst.samplestore.server.connect.idle.ms |  infinite |   Thrift API server connection idl (in ms)   |



---
------ [HOME](../../readme.md) --------------------------------------------
