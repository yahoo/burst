![Burst](../documentation/burst_h_small.png "")
--

![](./doc/supervisor.png "")

___Supervisor___ is the centralized dispatch process for incoming client requests dispatched to one or more ___Workers___
within the Burst ___Cell___. It will be possible to have more than one supervisor per cell in upcoming releases, but for now
this is a singular process on a specially designated supervisor node.

## Configuration

|  system property |  default |  description |
|---|---|---|
| burst.environment.properties.file |  None |  Environment properties file location  |

Rest
--

![](../burst-supervisor/doc/liaison.png "")

___Rest___ provides builtin [JAX-RS](https://jax-rs-spec.java.net/) JSON REST support via
the [Jersey](https://jersey.java.net/) reference implementation for all Burst services.

## Dependencies

* [Jersey JAX-RS](https://jersey.github.io/) -- JAX-RS implementation
* [Grizzly](https://javaee.github.io/grizzly/) -- HTTP/HTTPS server
* [Grizzly Websockets](https://javaee.github.io/grizzly/websockets.html) -- WEBSOCK support
* [Jackson](https://github.com/FasterXML/jackson) -- JSON support.

## Configuration

|  system property |  default |  description |
|---|---|---|
| burst.liaison.name |  'burst' |  user friendly name of application  |
| burst.fabric.http.host |  0.0.0.0 |  hostname/interface for REST API  |
| burst.fabric.http.port |  443 |  port for REST API  |
| burst.liaison.homepage |  "burst.html" |  home page for UI  |
| burst.fabric.http.keystore.path | "" | the keystore to use

## Overview

The Burst supervisor has a REST API that serves as both a set of HTTP/JSON end points ___and___ a server for
the [Burst Dash](../burst-dash/readme.md) browser application.

### Making local CLI requests

The keystore packaged with Burst contains an insecure, self-signed cert/key pair. Running the dash in this insecure
configuration will prevent the use of the CLI (or any other java client). If you wish to issue queries you will need to
run the dash using a certificate trusted by java. You can do with the property `burst.fabric.http.keystore.path`.

One way to generate an appropriate keystore is to use [mkcert](https://github.com/FiloSottile/mkcert):

```shell
brew install mkcert
mkcert localhost 127.0.0.1 ::1
openssl pkcs12 -export -in localhost+2.pem -inkey localhost+2-key.pem -out localhost.p12 -name burst-dash
```

---
------ [HOME](../readme.md) --------------------------------------------
