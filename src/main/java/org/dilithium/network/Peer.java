package org.dilithium.network;

import org.bouncycastle.util.encoders.Hex;
import org.dilithium.network.messages.uMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

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
                if(Arrays.equals(this.address, received.getSender())) {
                    send(serveType(received), serve(received));
                }
            }
        }
    }

    public byte[] serve(uMessage in) {
        return Peer2Peer.commands.get(in.getMessageType()).handle(in);
    }

    public int serveType(uMessage in) {
        return 1;
        /* TODO: Replace with actual message type handling */
    }

    public uMessage receive(DataInputStream in) {
        try {
            return new uMessage(in.readAllBytes());
        } catch (Exception e) {
            return null;
        }
    }

    public void send(int i, byte[] data) {
        try {
            if (data != null) {
                uMessage u = new uMessage(i, data, Peer2Peer.key.getPrivKeyBytes());

                byte[] toSend = u.getEncoded();
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
