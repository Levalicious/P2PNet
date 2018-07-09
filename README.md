# P2PNet
It's like libp2p, but not as good.

## To Do:
* ~~Implement Peer trimming (Remove dead peers periodically)~~ Peers are automatically removed upon disconnect.
* ~~Implement received message handling (Ignore previously seen messages)~~ Peer2Peer class contains a bloom filter which periodically retargets
* ~~Implement message relaying~~ Handled inside NetworkCommands
* ~~Implement peerset saving (upon bootup attempt to connect to previously seen peers)~~ Implemented
* ~~Implement random walk (bootstrap from connected nodes without forming cliques)~~  ~~Implemented~~ Removed.
* ~~Optional : Implement whisper protocol (message routing without flooding)~~ Implemented
* Optional : Implement alternate network topologies (standard mesh, hypercube, toroid)
* ~~Optional : Encrypt traffic between peers by default.~~ Implemented. However, sender & receiver are known to peers who relayed the message.
* Optional : Implement UPnP Support
* Implement more frequent peer checking - Removal of dead peers needs to be reliable.
* Implement IP ban list to ignore messages from.

## Usage: 
Navigate to the directory this repo was saved in and run 'mvn install'. You can import the resulting jar file to your project and initialize a network using the methods described below.

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


They are created as follows: Before initializing the network, create a set of classes extending NetworkCommand, one for each messagetype you want to handle.

Here are the interactions the handle() method can cause:

If it returns a value, that will be packaged and sent back directly to the peer who sent the message triggering the handling.

If it wishes to respond to a message by broadcasting, from within the method, call Peer2Peer.broadcast(message).

If it wishes to respond to a message by whispering, from within the method, call Peer2Peer.relay(message).


Once a Hashmap<Integer, NetworkCommand> has been created containing all the handlers one needs, initialize the Peer2Peer class.

Do so by running Peer2Peer <instancename> = new Peer2Peer(int desiredPort, ECKey() nodeKeyPair, int bucket-k-value, int sufficientPeers, HashMap<Integer, NetworkCommand> handlers);

The desired port is relatively self explanatory. 
The ECKey() class is packaged with the networking and creates a keypair for message signing, encryption, and Kademlia-like routing.
The k value is specific to Kademlia-style routing, which uses "buckets" instead of a large list of peers.
The sufficientPeers value allows the network to periodically (every 15 minutes) check to make sure it has a certain number of peers. If it doesn't have enough to satisfy this number, it'll attempt to connect to more. Past the sufficientPeer value, network churn should allow for a stable increase in peercounts, but the sufficientPeer check acts as a helper to the bootstrap mechanism.


## Credit
This library is based off of the code of [Carrdinal](https://github.com/Carrdinal) with assistance from [Soul](https://github.com/soulblade249). Also, the cryptographic portion of the code is taken from EthereumJ.
