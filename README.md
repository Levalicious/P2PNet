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

Considering this is for a cryptocurrency project of mine, here's a list of commands I've added for that:
| Byte Identifier | Name                              | Responses  |
|-----------------|-----------------------------------|------------|
| 0x06            | Ping                              | 0x07       |
| 0x07            | Pong                              |            |
| 0x10            | Transaction                       |            |
| 0x11            | Transaction List                  |            |
| 0x12            | Transaction Request               | 0x10       |
| 0x13            | Transaction List Request          | 0x11       |
| 0x20            | Block Header                      |            |
| 0x21            | Block Payload                     |            |
| 0x22            | Block                             |            |
| 0x23            | Block Header Request (by hash)    | 0x20       |
| 0x24            | Block Header Request (by height)  | 0x20       |
| 0x25            | Block Payload Request (by hash)   | 0x21       |
| 0x26            | Block Payload Request (by height) | 0x21       |
| 0x27            | Block Request (by hash)           | 0x22       |
| 0x28            | Block Request (by height)         | 0x22       |
| 0x40            | Chain Height                      |            |
| 0x41            | Chain Height Request              | 0x40       |
| 0x42            | Chain Head Request                | 0x22       |
| 0x43            | Chain Head Request (Header only)  | 0x20       |
| 0x44            | Chain Head Request (Payload only) | 0x21       |
| 0x45            | Chain Head Request (Block hash)   | 0x46       |
| 0x46            | Block Hash                        |            |
| 0x47            | Block Hash Request (by height)    | 0x46       |
| 0xF0            | Message                           |            |