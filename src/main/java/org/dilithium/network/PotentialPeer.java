package org.dilithium.network;

import org.dilithium.network.messages.uMessage;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.Arrays;

public class PotentialPeer extends Peer {
    Socket s;
    long initialized;
    byte[] address;

    protected PotentialPeer(Socket s) {
        super(null, s);
        this.s = s;
        this.initialized = System.currentTimeMillis();
    }

    public boolean hasAddress() {
        return (address != null);
    }

    public byte[] getAddress() {
        return address;
    }

    public Socket getSocket() {
        return s;
    }

    public boolean toDelete() {
        if ((System.currentTimeMillis() - this.initialized) > 1000) {
            if (!hasAddress()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public byte[] serve(uMessage in) {
        byte[] temp = in.getPayload();

        if (temp.length != 32) {
            return null;
        } else {
            this.address = temp;
            return Peer2Peer.key.getAddress();
        }
    }

    @Override
    public void run() {
        try {
            boolean running = true;

            while (running) {
                uMessage received = super.receive(new DataInputStream(s.getInputStream()));

                if (received != null) {
                    send(0, serve(received));
                }
            }
        } catch (Exception e) {

        }
    }
}
