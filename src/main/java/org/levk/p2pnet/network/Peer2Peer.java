package org.levk.p2pnet.network;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.levk.p2pnet.crypto.ecdsa.ECKey;
import org.levk.p2pnet.network.commands.NetworkCommand;
import org.levk.p2pnet.network.peerSet.PeerSet;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static org.levk.p2pnet.util.ByteUtil.ZERO_BYTE;

public class Peer2Peer extends Thread {
    protected static int port;
    private static boolean running;
    private static boolean initialized;

    public static SecureRandom rand;

    public static int sufficientPeerCount;

    private static ServerSocket server;
    public static PeerSet peers;

    protected static ECKey key;

    protected static HashMap<Integer, NetworkCommand> commands = new HashMap<>();

    /* How often the bloom filter retargets & resets in ms */
    private static final long retargetTime = 1800000;

    protected static int expectedMessages = 1000;
    protected static BloomFilter<byte[]> loopHandler = BloomFilter.create(Funnels.byteArrayFunnel(), expectedMessages, 0.0001);
    protected static AtomicInteger itemsInserted;
    protected static long lastCleared;

    public static Vector<Peer> waitList = new Vector<>();

    public Peer2Peer(int port, ECKey key, int k, int sufficientPeerCount, HashMap<Integer, NetworkCommand> messageHandlers) {
        try {
            this.port = port;
            this.peers = new PeerSet(key.getAddress(), k);
            this.peers.start();
            this.key = key;

            this.rand = SecureRandom.getInstance("SHA1PRNG");

            this.sufficientPeerCount = sufficientPeerCount;

            initializeBloom();

            this.commands = messageHandlers;

            server = new ServerSocket(this.port);

            this.initialized = true;
        } catch (Exception e) {
            this.initialized = false;
        }

    }

    private static void initializeBloom() {
        itemsInserted = new AtomicInteger(0);
        lastCleared = System.currentTimeMillis();
        (new Thread() {
            @Override
            public void run() {
                while (!Peer2Peer.interrupted()) {
                    handleFilter();
                }
            }
        }).start();
    }

    public static void connect(String s) {
        connect(s, port);
    }

    public static void connect(String s, int port) {
        try {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(s, port), 3000);

            Peer p = new Peer(sock);
            p.start();

            waitList.add(p);
            System.out.println("Peer connected.");

            p.send(0, ZERO_BYTE);
        } catch (Exception e) {
            System.out.println("Connection failed.");
        }
    }

    public static void broadcast(int n, byte[] in) {
        peers.broadcast(n, in);
    }

    public static void relay(int n, byte[] target, byte[] in) {

    }

    @Override
    public void run() {
        running = true;

        while (running) {
            try {
                Socket s = server.accept();

                if (s != null) {
                    Peer p = new Peer(s);

                    if (p.isInitialized()) {
                        p.start();

                        waitList.add(p);

                        p.send(0, ZERO_BYTE);

                        for (int i = 0; i < waitList.size(); i++) {
                            if (waitList.get(i).toDelete()) {
                                waitList.remove(i).interrupt();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("The server has failed.", e);
            }
        }
    }

    private static void handleFilter() {
        /* If half an hour has passed since the last retarget & clear */
        if ((System.currentTimeMillis() - lastCleared) >= retargetTime) {
            /* Perform filter size retargetting */
            if (itemsInserted.intValue() >= ((expectedMessages / 4) * 3)) {
                if (itemsInserted.intValue() >= expectedMessages) {
                    /* If the filter fully overloads, double size
                     * For panic moments, network under high stress etc*/
                    int stressLevel = (int)Math.ceil((double)itemsInserted.intValue() / (double)expectedMessages);
                    if (stressLevel > 1)
                    expectedMessages = expectedMessages * stressLevel;
                } else {
                    /* If the filter went over 75% capacity, boost size by 5% */
                    expectedMessages = (int)Math.ceil((double)expectedMessages * 1.05);
                }
            } else if (itemsInserted.intValue() <= ((expectedMessages / 4) * 2)) {
                /* IF the filter was below 50% capacity, decrease size by 5% */
                expectedMessages = (int)Math.floor((double)expectedMessages * 0.95);
            }

            if (expectedMessages < 100) expectedMessages = 100;

            /* Clear bloom filter */
            lastCleared = System.currentTimeMillis();
            loopHandler = BloomFilter.create(Funnels.byteArrayFunnel(), expectedMessages, ((double)1 / (double) expectedMessages));
            itemsInserted.set(0);
        }
    }

    public static String getPeers() {
        return peers.toString();
    }
}
