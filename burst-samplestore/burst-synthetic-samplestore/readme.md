## Synthetic Samplestore

The Syntetic Samplestore will use a generator to materialize a dataset on demand.  It uses the basic sample store client
to talk to a remote sample store using the nexus protocol. The remote synthetic samplestore supervisor and workers generate
content on demand and stream it back to the cell via the nexus streams.

| Property | Description | Default |
| -------- | ----------- | ------- |
| `synthetic.samplestore.loci.count` | The number of loci that should be generated | `0` |
| `synthetic.samplestore.use-localhost` | If the loci should have their hostname and ip address set to `localhost` and `127.0.0.1` set this property to true (currently there is no alternative allowed) | `true` |
| `synthetic.samplestore.variable-hash` | If the hash should be constant across loads set this property to `ture`, otherwise the hash will be newly generated GUID on each load | `true` |
| `synthetic.samplestore.press.dataset` | The name of the dataset. The only value corresponding to a built-in source is `synthetic-unity` | None |
| `synthetic.samplestore.press.timeout` | the amount of time to wait for all items to be pressed | `1 minute` |
| `synthetic.samplestore.press.item.max.bytes` | the max size for an item in bytes | `10e6` |
| `synthetic.samplestore.press.item.count` | the number of items to generate | `0` |
