# RingServer - Simple Distributed Cache Server

This is a very rudimentary caching server not for production environments. 

I wanted to learn the distributed system concepts and I prefer doing realtime projects instead of staying theoretical. I implemented leetcode question on Design LRU Cache and immediately idea clicked to make it distributed so that I get a chance to learn about the following in the order of priority:

* Horizontal Scaling Design (In progress)
* Consistent Hashing & sharding of data (In progress)
* Hands on with gRPC (Implemented already)
* LRU Cache with TTL (Implemented already)
* Leader election (Bully algorithm) - (Planned)
* Replication of data using Raft (Planned)

Ringserver isn't perfect for production workloads and not even useful for playing around with it, but it's good enough for me to learn about distributed system concepts described above and importantly being hands-on in addition to being only theoretical.

Java is my primary & best known programming language and I've chosen it to implement it. Ringserver would require Java 21 and above with a maven setup in local to play around. 

I will attempt to write a parallel project with _golang_ as it's fast becoming favorite programming language I love nowadays. But that's for another day to get real handson experience with _golang_.

Until then, happy learning!