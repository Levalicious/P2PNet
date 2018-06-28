package org.dilithium.network;

import org.bouncycastle.util.encoders.Hex;
import org.dilithium.network.messages.uMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static org.dilithium.network.Peer2Peer.commands;
import static org.dilithium.util.NetUtil.deblobify;
import static org.dilithium.util.NetUtil.semiblobify;

public class Peer extends Thread {
    private byte[] address;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean running;
    private boolean initialized;

    public Peer(byte[] address, Socket socket) {
        try {
            this.address = address;
            this.socket = socket;

            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());

            this.initialized = true;
        } catch (Exception e) {
            this.initialized = false;
        }
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

            if (received != null) {
                send(serveType(received), serve(received));
            }
        }
    }

    public byte[] serve(uMessage in) {
        if (commands.containsKey(in.getMessageType())) {
            return commands.get(in.getMessageType()).handle(in);
        } else {
            return null;
        }
    }

    public int serveType(uMessage in) {
        return 1;
        /* TODO: Replace with actual message type handling */
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

            System.out.println(Hex.toHexString(deblobify(blob)));

            return new uMessage(deblobify(blob));
        } catch (Exception e) {
            return null;
        }
    }

    public void send(int i, byte[] data) {
        try {
            if (data != null) {
                uMessage u = new uMessage(i, data, Peer2Peer.key.getPrivKeyBytes());

                byte[] message = u.getEncoded();

                System.out.println(Hex.toHexString(message));

                byte[] toSend = semiblobify(message);

                out.write(toSend.length);
                out.write(toSend);
                out.flush();
            }
        } catch (Exception e) {
            this.running = false;
        }
    }

    public String toString() {
        String s = "";
        s = s + Hex.toHexString(address) + ": ";
        s = s + socket.getInetAddress().toString() + ":" + socket.getPort() + "\n";

        return s;
    }

}
