package org.dilithium.network;

import com.google.common.primitives.UnsignedInteger;
import org.bouncycastle.util.encoders.Hex;
import org.dilithium.network.messages.uMessage;
import org.dilithium.util.BIUtil;
import org.dilithium.util.ByteUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import static org.dilithium.util.ByteUtil.concat;

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
            byte[] lengthBytes = new byte[4];

            int count = in.read(lengthBytes);

            if(count != 4) {
                throw new RuntimeException("Reading in sucks");
            } else {
                UnsignedInteger length = UnsignedInteger.valueOf(Hex.toHexString(ByteUtil.stripLeadingZeroes(lengthBytes)), 16);

                byte[] data = new byte[length.intValue()];

                count = in.read(data);

                if(count != data.length) {
                    throw new RuntimeException("Reading in sucks");
                } else {
                    return new uMessage(data);
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void send(int i, byte[] data) {
        try {
            if (data != null) {
                uMessage u = new uMessage(i, data, Peer2Peer.key.getPrivKeyBytes());

                byte[] toSend = u.getEncoded();

                if(toSend.length > Integer.MAX_VALUE || toSend.length < 0) {
                    System.out.println("Packet size is drunk.");
                    throw new RuntimeException("Fuck.");
                }

                UnsignedInteger length = UnsignedInteger.valueOf(toSend.length);

                byte[] lengthBits = Hex.decode(length.toString(16));

                if (lengthBits.length > 4) {
                    System.out.println("Packet size is outside of safely drunk parameters.");
                    throw new RuntimeException("Fuck.");
                } else {
                    while (lengthBits.length < 4) {
                        lengthBits = concat(ByteUtil.ZERO_BYTE, lengthBits);
                    }
                }

                toSend = concat(lengthBits, toSend);

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
