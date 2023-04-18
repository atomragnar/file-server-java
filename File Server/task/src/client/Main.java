package client;

import java.io.*;


public class Main {

    public static void main(String[] args) throws InterruptedException {

        final String address = "127.0.0.1";
        final int port = 23456;

        // Thread.sleep(1000);

        try {
            ClientSession clientSession = new ClientSession(address, port);
            clientSession.start();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
