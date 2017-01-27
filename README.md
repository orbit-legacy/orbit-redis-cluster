Orbit Redis Cluster Implementation
============
[![Release](https://img.shields.io/github/release/orbit/orbit-redis-cluster.svg)](https://github.com/orbit/orbit-redis-cluster/releases)
[![Maven Central](https://img.shields.io/maven-central/v/cloud.orbit/orbit-redis-cluster.svg)](https://repo1.maven.org/maven2/cloud/orbit/orbit-redis-cluster/)
[![Javadocs](https://img.shields.io/maven-central/v/cloud.orbit/orbit-redis-cluster.svg?label=javadocs)](http://www.javadoc.io/doc/cloud.orbit/orbit-redis-cluster)
[![Build Status](https://img.shields.io/travis/orbit/orbit-redis-cluster.svg)](https://travis-ci.org/orbit/orbit-redis-cluster)
[![Gitter](https://img.shields.io/badge/style-Join_Chat-ff69b4.svg?style=flat&label=gitter)](https://gitter.im/orbit/orbit?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

_Warning: This is a very crude first pass prototype and is not intended for use. There are many optimizations still to be performed._

Example Usage
=============

URI Format: redis://host:port

```java
final RedisClusterPeer clusterPeer = new RedisClusterBuilder()
    .actorDirectoryUri(actorDirUri, actorDirClustered)
    .nodeDirectoryUri(nodeDirUri, nodeDirClustered)
    .messagingUris(Arrays.asList(
                    messagingUri1,
                    messagingUri2
    ))
    .build();

final Stage stage = new Stage.Builder()
    .clusterName("myCluster")
    .clusterPeer(clusterPeer)
    .build();
```

Developer & License
======
This project was developed by [Electronic Arts](http://www.ea.com) and is licensed under the [BSD 3-Clause License](LICENSE).
