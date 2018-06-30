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
* Optional : Implement UPnP Support


## Commands:

### Internal:
| Byte Identifier | Name              | Responses  |
|-----------------|-------------------|------------|
| 0x00            | Join              | 0x02, 0x03 |
| 0x01            | Leave             |            |
| 0x02            | Yes               |            |
| 0x03            | No                |            |
| 0x04            | Peer List         |            |
| 0x05            | Peer List Request | 0x04       |

### External:
External commands are highly configurable. One can add a new one at any point in time, and, provided they also create the structure for handling it, it will function. 
