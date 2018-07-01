package org.levk.p2pnet.network.peerSet;

import org.levk.p2pnet.network.Peer;
import org.levk.p2pnet.util.Tuple;
import org.levk.p2pnet.util.serialization.RLP;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.stream.IntStream;

import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.xor;
import static org.levk.p2pnet.network.Peer2Peer.rand;
import static org.levk.p2pnet.network.Peer2Peer.sufficientPeerCount;
import static org.levk.p2pnet.util.ByteUtil.ZERO_BYTE;

public class PeerSet extends Thread {
    private boolean running;
    private byte[] rlpEncoded;
    private boolean parsed;

    private int peerCount;

    private int k;
    private Bucket[] buckets = new Bucket[256];
    public static byte[] nodeAddress;

    public PeerSet(byte[] nodeAddress, int k) {
        this.nodeAddress = nodeAddress;
        peerCount = 0;
        this.k = k;
        running = true;
    }

    public boolean add(Peer p) {
        int bucketIndex = calcDist(nodeAddress, p.getAddress()).indexOf('1');

        if(bucketIndex >= 0) {
            if(buckets[bucketIndex] == null) {
                buckets[bucketIndex] = new Bucket(k, nodeAddress);
            }

            if (buckets[bucketIndex].add(p)) {
                parsed = false;
                peerCount++;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean contains(Peer p) {
        int bucketIndex = calcDist(nodeAddress, p.getAddress()).indexOf('1');

        if(bucketIndex >= 0) {
            if(buckets[bucketIndex] == null) {
                return false;
            }

            return buckets[bucketIndex].contains(p);
        } else {
            return false;
        }
    }

    public boolean remove(Peer p) {
        int bucketIndex = calcDist(nodeAddress, p.getAddress()).indexOf('1');

        if(bucketIndex >= 0) {
            if(buckets[bucketIndex] == null) {
                return false;
            }

            if (buckets[bucketIndex].remove(p)) {
                parsed = false;
                peerCount--;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void broadcast(int messagetype, byte[] message) {
        for(int i = 0; i < 256; i++) {
            if(buckets[i] != null) {
                buckets[i].broadcast(messagetype, message);
            }
        }
    }

    public void relay(int messagetype, byte[] target, byte[] message) {

        int bucketIndex = findClosest(target);

        if (bucketIndex >= 0) {
            buckets[bucketIndex].relay(messagetype, target, message);
        }
    }

    private int findClosest(byte[] target) {
        int targetBucket = calcDist(nodeAddress, target).indexOf('1');

        if (targetBucket >= 0) {
            if (buckets[targetBucket] != null && buckets[targetBucket].hasPeers()) {
                return targetBucket;
            }

            for(int i = 1; i <= 128; i++) {
                /* Identify the indices of the two closest buckets */
                int high = targetBucket + i;
                int low = targetBucket - i;

                /* If the indices go out of the bounds of the array, set them at the boundary. */
                if(low < 0) low = 0;
                if(high > 255) high = 255;

                /* Establish a peer count for each bucket */
                int highCount = 0;
                int lowCount = 0;

                /* If the next closest bucket exists, grab the peercount from it */
                if(buckets[high] != null) {
                    highCount = buckets[high].getPeerCount();
                }

                /* If the next farthest bucket exists, grab the peercount from it */
                if(buckets[low] != null) {
                    lowCount = buckets[low].getPeerCount();
                }

                /* If at least one of the two buckets targeted has at least one peer */
                if(highCount > 0 || lowCount > 0) {
                    /* Identify the bucket containing the most peers and return that bucket */
                    if(highCount > lowCount) {
                        return high;
                    } else {
                        return low;
                    }
                }
            }
        }

        return -1;
    }

    public static String calcDist(byte[] from, byte[] to) {
        return fromBitSet(fromByteArray(xor(from, to)));
    }

    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static String fromBitSet(BitSet set) {
        final StringBuilder buffer = new StringBuilder(set.length());
        IntStream.range(0, set.length()).mapToObj(i -> set.get(i) ? '1' : '0').forEach(buffer::append);
        String temp = buffer.reverse().toString();
        while(temp.length() < 256) {
            temp = 0 + temp;
        }

        return temp;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < 256; i++) {
            if(buckets[i] != null) {
                s = s + buckets[i].toString();
            }
        }

        return s;
    }

    public byte[] serialize() {
        if (parsed) return  rlpEncoded;

        ArrayList<byte[]> peerlist = new ArrayList<>();

        for(int i = 0; i < 256; i++) {
            if(buckets[i] != null) {
                peerlist.addAll(buckets[i].serialize());
            }
        }

        this.parsed = true;
        this.rlpEncoded = RLP.encodeList();
        return rlpEncoded;
    }

    public int getPeerCount() {
        return peerCount;
    }

    public Peer getRandom(int seed, int seed2) {
        if (peerCount > 1) {
            int target = seed % buckets.length;

            for (int i = 0; i < buckets.length; i++) {
                if (buckets[target + i] != null) {
                    if (buckets[target + i].hasPeers()) {
                        return buckets[target + i].getRandom(seed2);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(900000);

                if (getPeerCount() < sufficientPeerCount) {
                    Peer p = getRandom(rand.nextInt(), rand.nextInt());

                    if (p != null) {
                        p.send(new Tuple(0x05, ZERO_BYTE));
                    }
                }
            }
        } catch (Exception e) {

        }
    }
}
