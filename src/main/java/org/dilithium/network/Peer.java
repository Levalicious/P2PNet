package org.dilithium.network;

import org.bouncycastle.util.encoders.Hex;
import org.dilithium.network.messages.uMessage;
import org.dilithium.util.Tuple;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import static org.dilithium.network.Peer2Peer.commands;
import static org.dilithium.network.Peer2Peer.waitList;
import static org.dilithium.util.ByteUtil.ONE_BYTE;
import static org.dilithium.util.ByteUtil.ZERO_BYTE;
import static org.dilithium.util.NetUtil.deblobify;
import static org.dilithium.util.NetUtil.semiblobify;

public class Peer extends Thread {
    private byte[] address;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean running;
    private boolean initialized;
    private long firstSeen;

    public Peer(Socket socket) {
        this(null, socket);
    }

    public Peer(byte[] address, Socket socket) {
        try {
            this.address = address;
            this.socket = socket;

            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());

            this.firstSeen = System.currentTimeMillis();

            this.initialized = true;
        } catch (Exception e) {
            this.initialized = false;
        }
    }

    public boolean hasAddress() {
        return (address != null);
    }

    public byte[] getAddress() {
        return address;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            uMessage received = receive(in);

            try {
                received.getSig();
            } catch (Exception e) {
                received = null;
            }

            if (received != null) {
                if (!this.hasAddress()) {
                    this.address = received.getSender();
                    System.out.println("Set address to " + Hex.toHexString(this.address));
                }

                send(serve(received));
            }
        }
    }

    public Tuple<Integer, byte[]> serve(uMessage in) {
        /* Handling join request */
        if (in.getMessageType() == 0x02) {
            if (Peer2Peer.peers.contains(this)) {
                return new Tuple(0x04, ZERO_BYTE);
            } else {
                if (Peer2Peer.peers.add(this)) {
                    Peer2Peer.waitList.remove(this);
                    return new Tuple(0x04, ZERO_BYTE);
                } else {
                    Peer2Peer.waitList.remove(this);
                    return new Tuple(0x05, ZERO_BYTE);
                }
            }
        }

        /* Handling yes messages */
        if (in.getMessageType() == 0x04) {
            if (Arrays.equals(in.getPayload(), ZERO_BYTE)) {
                if (Peer2Peer.peers.add(this)) {
                    Peer2Peer.waitList.remove(this);
                    return new Tuple(0x04, ONE_BYTE);
                } else {
                    if (!Peer2Peer.peers.contains(this)) {
                        return new Tuple(0x05, ZERO_BYTE);
                    } else {
                        Peer2Peer.waitList.remove(this);
                        return new Tuple(0x04, ONE_BYTE);
                    }
                }
            }
        }

        /* Handling no messages */
        if (in.getMessageType() == 0x05) {
            Peer2Peer.waitList.remove(this);
            Peer2Peer.peers.remove(this);
            this.interrupt();
        }

        /* Handling leave request */
        if (in.getMessageType() == 0x03) {
            Peer2Peer.peers.remove(this);
            waitList.remove(this);
            this.interrupt();
        }

        /* Handling peer request */
        /* TODO: Serialize peers & send */



        if (commands.containsKey(in.getMessageType())) {
            return commands.get(in.getMessageType()).handle(in);
        } else {
            return null;
        }
    }

    public uMessage receive(DataInputStream in) {
        try {
            byte[] chunk;

            ArrayList<byte[]> blob = new ArrayList<>();

            int count;
            do {
                chunk = new byte[32];
                count = in.read(chunk);

                if (count != 32) {
                    return null;
                }

                blob.add(chunk);
            } while (chunk[0] == 0);

            System.out.println("Received: " + new uMessage(deblobify(blob)).getMessageType() + " : " + Hex.toHexString(deblobify(blob)));

            return new uMessage(deblobify(blob));
        } catch (Exception e) {
            return null;
        }
    }

    public void send(int messagetype, byte[] data) {
        this.send(new Tuple(messagetype, data));
    }

    public void send(Tuple<Integer, byte[]> s) {
        try {
            int i = -1;
            byte[] data = null;

            if (s != null) {
                i = s.x;
                data = s.y;
            }

            if (data != null && i != -1) {
                uMessage u = new uMessage(i, data, Peer2Peer.key.getPrivKeyBytes());

                byte[] message = u.getEncoded();

                System.out.println("Sending: " + Hex.toHexString(message));

                byte[] toSend = semiblobify(message);

                out.write(toSend.length);
                out.write(toSend);
                out.flush();
            }
        } catch (Exception e) {
            this.running = false;
        }
    }

    public boolean toDelete() {
        if ((System.currentTimeMillis() - this.firstSeen) > 1000) {
            if (!hasAddress()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        String s = "";
        s = s + Hex.toHexString(address) + ": ";
        s = s + socket.getInetAddress().toString() + ":" + socket.getPort() + "\n";

        return s;
    }
}
