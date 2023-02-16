# Base Samplestore

The Base Samplestore package contains the basic structure for a remote sample store serving responding the samplestore api
and streaming load requests over the nexus protocol.

| Property | Description | Default |
| -------- | ----------- | ------- |
| `synthetic.samplestore.variable-hash` | If the hash should be constant across loads set this property to `ture`, otherwise the hash will be newly generated GUID on each load | `true` |
| `synthetic.samplestore.press.dataset` | The name of the dataset. The only value corresponding to a built-in source is `synthetic-unity` | None |
| `synthetic.samplestore.press.timeout` | the amount of time to wait for all items to be pressed | `1 minute` |
| `synthetic.samplestore.press.item.max.bytes` | the max size for an item in bytes | `10e6` |
| `synthetic.samplestore.press.item.count` | the number of items to generate | `0` |
