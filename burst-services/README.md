![Burst](../documentation/burst_h_small.png "")
# burst-services

## Docker

Burst publishes a docker image `burst` designed to be used as a base image for a cell deployment. The docker image
is based on `amazoncorretto:8` and includes burst source.

Required env vars:
- DB_HOST - The database host
- DB_PASS - The password to the database host
- DEPLOY_ENV - A distinguishable name that will be present in the logs
- WORKLOAD - Should be one of `supervisor` or `worker`, a cell requires a single `supervisor` that coordinates many `worker`s
- SAMPLESTORE_HOST - The samlestore master hostname. Required only for `supervisor`.
- BURST_SUPERVISOR_HOST - The supervisor hostname (or k8s service). Required only for `worker`s

Other configurable env vars:
- CPU_REQUEST - The number of cores that the `worker` should use (determines thread pool sizes)
- DB_USER - The user for the database, defaults to `burst`
- EXTRA_JAVA_OPTS - A place to inject custom java options
- FAB_MONIKER
- JAVA_NMT_OPTS - Custom memory tracking args. Defaults to  `-XX:NativeMemoryTracking=detail -XX:+PrintNMTStatistics`
- JVM_DIRECT - The value in `G` of direct memory to passed to `-XX:MaxDirectMemorySize`
- JVM_HEAP - The value in `G` passed to `Xmx`
- KEEPALIVE - If set to any non-empty value then the container will not exit after the JVM dies
- KEYSTORE_FILE - they keystore used by the web server
- KEYSTORE_PASS - they password for the web keystore
- SSL_CERT_BUNDLE_PATH - The path to the X509 CA cert bundle
- SSL_CERT_PATH - The path to the X509 public cert
- SSL_KEY_PATH - The path to the X509 private key
- LOG_LEVEL - The log4j level. Defaults to `INFO`

Users can customize these environment variables either before invoking `start.sh` or by placing a `pre-start.sh` script
at `$BURST_HOME/sbin/pre-start.sh`. (`BURST_HOME` is set to `/app/burst` in the docker container). The pre-start script
is sourced by the start script.

The directory `$BURST_HOME/classpath-files` is added to the classpath at runtime and can be used to override

## Shading

Burst publishes `burst-services` a shaded JAR that can be directly executed.
