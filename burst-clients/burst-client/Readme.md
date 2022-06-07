# Burst Client

## Developing

### Updating thrift definitions

```shell
#install thrift 0.12
cd burst-client
thrift -out src/main/java  -I src/main/thrift --gen java -r src/main/thrift/clientService.thrift
```
