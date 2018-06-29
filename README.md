# NetTests

## To Do:
* ~~Implement Peer trimming (Remove dead peers periodically)~~ Peers are automatically removed upon disconnect.
* ~~Implement received message handling (Ignore previously seen messages)~~ Peer2Peer class contains a bloom filter which periodically retargets
* Implement message relaying
* Implement peerset saving (upon bootup attempt to connect to previously seen peers)
* Implement random walk (bootstrap from contected nodes without forming cliques)
* Optional : Implement whisper protocol (message routing without flooding)
* Optional : Implement alternate network topologies (standard mesh, hypercube, toroid)




