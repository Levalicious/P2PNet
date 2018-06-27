package org.dilithium;

import org.dilithium.crypto.ecdsa.ECKey;
import org.dilithium.network.Peer2Peer;

import java.util.Scanner;

public class Start {
    public static void main(String[] args)  {
        try {
            ECKey key = new ECKey();
            Peer2Peer net = new Peer2Peer(40424, key, 6);
            net.start();

            Scanner s = new Scanner(System.in);

            boolean running = true;

            System.out.println("System initialized.");
            System.out.println("To send a message, enter '1'.\n" +
                    "To connect to a peer, enter '2'. \n" +
                    "To list currently connected peers, enter '3'.\n" +
                    "To exit the program, enter '4'.");
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
                } else if(choice == 3) {
                    System.out.println("Current Peers: ");
                    System.out.println(net.getPeers());
                } else if(choice == 4) {
                    System.out.println("Shutting down.");
                    running = false;
                }
            }

            System.exit(666);
        } catch (Exception e) {
            System.out.println("Failed.");
        }
    }
}
