package org.dilithium.network;

import org.dilithium.crypto.ecdsa.ECKey;
import org.dilithium.network.commands.NetworkCommand;
import org.dilithium.network.commands.TextCommand;
import org.dilithium.network.messages.uMessage;
import org.dilithium.network.peerSet.PeerSet;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Peer2Peer extends Thread {
    private int port;
    private boolean running;
    private boolean initialized;

    private ServerSocket server;
    private PeerSet peers;

    protected static ECKey key;

    protected static HashMap<Integer, NetworkCommand> commands = new HashMap<>();

    private ArrayList<PotentialPeer> waitList = new ArrayList<>();


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
            sock.connect(new InetSocketAddress(s, port), 1000);
            System.out.println("Socket connected");

            PotentialPeer p = new PotentialPeer(sock);
            System.out.println("PotentialPeer initialized");
            p.start();
            System.out.println("PotentialPeer Started");

            this.waitList.add(p);
            System.out.println("PotentialPeer Added to Waitlist");

            p.send(1, key.getAddress());
            System.out.println("Connection Message Sent");
        } catch (Exception e) {
            System.out.println("Connection timed out.");
        }
    }

    private void initializeCommands() {
        commands.put(1, new TextCommand());
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
                    PotentialPeer p = new PotentialPeer(s);

                    p.start();

                    waitList.add(p);

                    p.send(1, key.getAddress());

                    for (int i = 0; i < waitList.size(); i++) {
                        if (waitList.get(i).toDelete()) {
                            waitList.remove(i).stop();
                        } else if (waitList.get(i).hasAddress()) {
                            PotentialPeer pot = waitList.remove(i);
                            Peer newPeer = new Peer(pot.getAddress(), pot.getSocket());
                            newPeer.start();
                            peers.add(newPeer);
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
