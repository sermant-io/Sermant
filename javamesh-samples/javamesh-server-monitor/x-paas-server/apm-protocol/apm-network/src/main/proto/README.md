# Apache SkyWalking data collect protocol
Apache SkyWalking typically collect data from 
1. Language agent, including Java, .Net, PHP, Golang, NodeJS, Lua.
1. Service mesh proxy, especially Envoy controlled by Istio
1. Meter data, also known pure metrics data.
1. 3rd libs, such as Zipkin, Jaeger

This repo hosts the protocol of SkyWalking native report protocol, defined in gRPC. Read [Protocol DOC](https://github.com/apache/skywalking/blob/master/docs/en/protocols/README.md#probe-protocols) for more details

## Release
This repo wouldn't release separately. All source codes have been included in the main repo release. The tags match the [main repo](https://github.com/apache/skywalking) tags.

## License
Apache 2.0
