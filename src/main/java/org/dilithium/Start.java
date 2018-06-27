package org.dilithium;

import org.bouncycastle.util.encoders.Hex;
import org.dilithium.crypto.ecdsa.ECKey;
import org.dilithium.network.Peer2Peer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import static org.dilithium.crypto.Hash.keccak256;

public class Start {
    public static void main(String[] args)  {
        try {
            ECKey key = new ECKey();
            Peer2Peer net = new Peer2Peer(40424, key, 6);
            net.start();

            Scanner s = new Scanner(System.in);

            boolean running = true;

            while (running) {
                int choice = s.nextInt();
                if(choice == 1) {
                    net.broadcast(1, s.next().getBytes("UTF-8"));
                } else if(choice == 2) {
                    System.out.print("Enter IP: ");
                    String ip = s.next();

                    System.out.print("Enter port: ");
                    int port = s.nextInt();

                    net.connect(ip, port);

                    System.out.println("Attempting connection.");
                } else if(choice == 3) {
                    System.out.println("Current Peers: ");
                    System.out.println(net.getPeers());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed.");
        }
    }
}
