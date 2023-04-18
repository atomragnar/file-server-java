package server;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        final String address = "127.0.0.1";
        final int port = 23456;

        new FileServer(address, port).run();

    }


}