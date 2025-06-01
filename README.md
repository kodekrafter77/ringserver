# RingServer - Simple Distributed Cache Server

This is a very rudimentary caching server not for production environments. 

I wanted to learn the distributed system concepts and I prefer doing realtime projects instead of staying theoretical. I implemented leetcode question on Design LRU Cache and immediately idea clicked to make it distributed so that I get a chance to learn about the following in the order of priority:

1. Horizontal Scaling Design (In progress)
2. Consistent Hashing & sharding of data (In progress)
3. Hands on with gRPC (Implemented already)
4. LRU Cache with TTL (Implemented already)
5. Leader election (Bully algorithm) - (Planned)
6. Replication of data using Raft (Planned)
7. Automated deployments using Jenkins (Planned)
8. Containerized cluster with Kubernetes (Planned)
9. Automated deployment to AWS EKS & GCP GKE (Planned)
10. Observability using Grafana & OpenTelemetry (Planned)
11. Distributed tracing using Jaeger or Zipkin (Planned)

Note that 9, 10 & 11 as above are stretch goals, but definitely given a chance to be tried.

Ringserver isn't perfect for production workloads and not even useful for playing around with it, but it's good enough for me to learn about distributed system concepts described above and importantly being hands-on in addition to being only theoretical.

Java is my primary & best known programming language and I've chosen it to implement it. Ringserver would require Java 21 and above with a maven setup in local to play around. 

I will attempt to write a parallel project with _golang_ as it's fast becoming favorite programming language I love nowadays. But that's for another day to get real handson experience with _golang_.

Until then, happy learning!

## Service Specification

Here is the Service/API Specification:

```protobuf
message SetRequest {
  string key = 1;
  string value = 2;
  int64 ttl = 3; // 0 means no ttl
}

message SetResponse {
  bool success = 1;
  string message = 2;
}

message GetRequest {
  string key = 1;
}

message GetResponse {
  string value = 1;
  bool found = 2;
}

service LRUCacheService {
  rpc Set(SetRequest) returns (SetResponse);
  rpc Get(GetRequest) returns (GetResponse);
}
```

## How to build and run?
Assuming Java 21+ and maven installed and the respective bin directories are in PATH system variable. 

To build:
```
$ mvn clean install
```
To run:
```
$ java target/ringserver.jar
```