![Burst](../../../../doc/burst_small.png)

# `Burst Language Architecture`


## Multi stage pipeline
![](../../../image/burst_transpilation.svg "")
Burst made a valuable early decision to separate the execution pipeline into multiple
distinct language based processing
steps. The first step is occupied by a high-level `SQL` familiar language called `EQL`.
The second step is a low level execution language called `HYDRA`.
The `EQL` subsystem fields  incoming external client analysis queries in the `EQL` language
and generated corresponding `HYDRA` transcompilation as an output. The `HYDRA` subsystem
takes incoming `HYDRA` language requests and generates `SCALA` transcompilation as an output.
Finally the `SCALA` is compiled down to bytecode and the bytecode is used to implement
high performance `JIT` warmed scan closures that execute the intended semantics of the incoming
`EQL` construction.

## The pipeline runtime
![](../../../image/burst_pipeline.svg "")
The sequence of operations as depicted above are:
1. **MASTER:**
    1. EQL source is received via the client library
    2. EQL `parses` the source and `validates` against the schema
    3. EQL `generates` HYDRA source
    4. HYDRA `parses` source and `validates` against the schema
    5. HYDRA `normalizes` the source
2. **NETWORK:**
    1. Hydra and Slice specs are serialized/scattered to worker-nodes
3. **WORKER:**
    1. Hydra checks sweep cache for generated artifacts
    2. Hydra `parses` the normalized source
    3. Hydra `generates` a Scala `Traveler` for the schema if needed
    4. Hydra `generates` a Scala `Sweep` that analysis if needed
    5. Scala compiler creates byte code for  Traveler and Sweep if needed
    6. Slice is loaded or fetched through the slice cache if needed
    7. Traveler and Sweep are used to scan all the regions in the slice
4. **NETWORK:**
    1. all worker slice merge results are serialized and sent back to master node
5. **MASTER:**
    1. slice results from all workers are merged together
    2. final result is sent back via the client library

