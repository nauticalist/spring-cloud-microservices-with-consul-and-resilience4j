# spring-cloud-microservices-with-consul-and-resilience4j

Sample spring cloud microservices application with Consul and Resilience4J

TODO: dockerize project

To run consul locally:

```
brew install consul
consul agent -server=true -bootstrap=true -ui -client=0.0.0.0 -bind=192.168.1.4 -data-dir=/tmp/consul &
```
