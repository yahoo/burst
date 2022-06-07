![Burst](../../../doc/burst_small.png) ![](../hydra_small.png "")
--
     
![](execution.png "")

# Execution Steps
__Execution__ for Hydra is a pipeline consists of the following processing sequence:
1. **Master Node**
   1. Receive language processing request from the [`Burst Agent`](../../../burst-agent/readme.md)
   2. Parse/validate the attached _analysis_ source
      1. return error messages if necessary...
   3. Create a [`FabricWave`](../../../burst-fabric/doc/waves.md) to manage the execution
   4. Attach one [`FabricParticle`](../../../burst-fabric/doc/waves.md) for each data slice/worker node in the target dataset
   5. Attach the appropriate [`FabricSlice`](../../../burst-fabric/doc/slice.md) data specification to each Particle
   6. Attach the `HydraScanner` with the _normalized source_ version of the resulting [`FeltTree`](../../../burst-felt/readme.md) to all the particles
   7. Serialize and transmit all the `FabricParticle` instances in the `FabricWave` to the appropriate Worker nodes via the `Fabric Protocol`
2. **Worker Node**
   1. Deserialize the `FabricParticle` along with the contained `FabricSlice` and `HydraScanner`
   2. Get `FabricSlice` data from `Fabric Data Cache`
   3. Look in local Felt artifactory cache for Felt Traveler and Felt Sweep Cache
      1. If needed, generate Traveler scala code and compile/cache
      2. If needed, generate Sweep scala code and compile/cache
   4. Execute scan using Traveler/Sweep across all Fabric Regions in the Slice producing a final Hydra Gather
   5. Serialize `HydraGather` and transmit to **Master** via **Fabric Protocol**
3. **Master Node**
   1. Deserialize the `HydraGather` and check for errors
   2. Return results or errors to **Agent**
   
# Design Notes
* Both the `FeltMaster` node and the `FeltWorker` node parse the Hydra source but scala code generation and compilation happens
 only on the `FeltWorker` node.
* Hydra is a concrete syntax for a **FELT** based language. This means **Hydra** is mostly a parser, a **FELT** tree generator, and a front end
    to allow execution of **Hydra** source language in the [`burst-agent`](../../../burst-agent/readme.md) processing framework

---
------ [UP](../../../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
