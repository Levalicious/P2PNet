# NetTests

## To Do:
* ~~Implement Peer trimming (Remove dead peers periodically)~~ Peers are automatically removed upon disconnect.
* ~~Implement received message handling (Ignore previously seen messages)~~ Peer2Peer class contains a bloom filter which periodically retargets
* ~~Implement message relaying~~ Handled inside NetworkCommands
* Implement peerset saving (upon bootup attempt to connect to previously seen peers)
* ~~Implement random walk (bootstrap from contected nodes without forming cliques)~~ Implemented
* ~~Optional : Implement whisper protocol (message routing without flooding)~~ Implemented
* Optional : Implement alternate network topologies (standard mesh, hypercube, toroid)
* ~~Optional : Encrypt traffic between peers by default.~~ Implemented. However, sender & receiver are known to peers who relayed the message.