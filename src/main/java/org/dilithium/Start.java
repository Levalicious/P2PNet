package org.dilithium;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.google.common.primitives.UnsignedInteger;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Hex;
import org.dilithium.crypto.ecdsa.ECKey;
import org.dilithium.network.Peer;
import org.dilithium.network.Peer2Peer;
import org.dilithium.network.messages.uMessage;
import org.dilithium.util.ByteUtil;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Start {
    public static void main(String[] args) throws Exception {
        try {
            ECKey key = new ECKey();
            System.out.println(Hex.toHexString(key.getAddress()));
            Peer2Peer net = new Peer2Peer(40424, key, 6);
            net.start();

            Scanner s = new Scanner(System.in);

            boolean running = true;

            System.out.println("System initialized.");
            System.out.println("To send a message, enter '1'.\n" +
                    "To connect to a peer, enter '2'. \n" +
                    "To list currently connected peers, enter '3'.\n" +
                    "To check how many peers are in the waitlist, enter '4'.\n" +
                    "To exit the program, enter '5'.");

            while (running) {
                int choice = s.nextInt();
                s.nextLine();
                if(choice == 1) {
                    System.out.print("Enter the message you'd like to send: ");
                    String sending = s.nextLine();
                    net.broadcast(0xF0, sending.getBytes("UTF-8"));
                } else if(choice == 2) {
                    System.out.print("Enter IP: ");
                    String ip = s.next();

                    System.out.print("Enter port: ");
                    int port = s.nextInt();

                    net.connect(ip, port);
                } else if(choice == 3) {
                    System.out.println("Current Peers: ");
                    System.out.println(net.getPeers());
                } else if(choice == 4) {
                    System.out.println("Waitlist Count: ");
                    System.out.println(Peer2Peer.waitList.size());
                } else if(choice == 5) {
                    System.out.println("Shutting down.");
                    running = false;
                }

                System.out.println();
            }

            System.exit(666);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed.");
        }
    }
}
