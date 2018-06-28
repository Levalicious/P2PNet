package org.dilithium.network;

import org.dilithium.crypto.ecdsa.ECKey;
import org.dilithium.network.commands.NetworkCommand;
import org.dilithium.network.commands.TextCommand;
import org.dilithium.network.peerSet.PeerSet;

import java.net.*;
import java.util.Vector;
import java.util.HashMap;

import static org.dilithium.util.ByteUtil.ZERO_BYTE;

public class Peer2Peer extends Thread {
    private int port;
    private boolean running;
    private boolean initialized;

    private static ServerSocket server;
    public static PeerSet peers;

    protected static ECKey key;

    protected static HashMap<Integer, NetworkCommand> commands = new HashMap<>();

    public static Vector<Peer> waitList = new Vector<>();


    public Peer2Peer(int port, ECKey key, int k) {
        try {
            this.port = port;
            this.peers = new PeerSet(key.getAddress(), k);
            this.key = key;

            initializeCommands();

            server = new ServerSocket(this.port);

            this.initialized = true;
        } catch (Exception e) {
            this.initialized = false;
        }
    }

    public void connect(String s, int port) {
        try {
            Socket sock = new Socket();
            System.out.println("Socket created");
            sock.connect(new InetSocketAddress(s, port), 3000);
            System.out.println("Socket connected");

            Peer p = new Peer(sock);
            System.out.println("Peer initialized");
            p.start();
            System.out.println("Peer Started");

            this.waitList.add(p);
            System.out.println("Peer Added to Waitlist");
        } catch (Exception e) {
            System.out.println("Connection failed.");
        }
    }

    private void initializeCommands() {
        commands.put(0xF0, new TextCommand());
    }

    public void broadcast(int n, byte[] in) {
        peers.broadcast(n, in);
    }

    @Override
    public void run() {
        running = true;

        while (running) {

            try {
                Socket s = server.accept();

                if (s != null) {
                    Peer p = new Peer(s);

                    p.start();

                    waitList.add(p);

                    p.send(2, ZERO_BYTE);

                    for (int i = 0; i < waitList.size(); i++) {
                        if (waitList.get(i).toDelete()) {
                            waitList.remove(i).interrupt();
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("The server has failed.", e);
            }
        }
    }

    public String getPeers() {
        return peers.toString();
    }
}
