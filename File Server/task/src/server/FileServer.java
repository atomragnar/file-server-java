package server;

import server.utils.FileUtils;
import server.utils.IdGenerator;
import shared.requests.ClientRequest;
import shared.responses.ServerResponse;
import shared.utils.SerializationUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer implements Runnable {
    private static final int OK = 200;
    private static final int NOT_FOUND = 404;
    private static final int FORBIDDEN = 403;
    private final String address;
    private final int port;
    private volatile boolean isStopped;
    private ServerSocket serverSocket;
    private final ExecutorService executorService;
    private static ConcurrentHashMap<Long, String> idToNameMap;

    public FileServer(String address, int port) throws IOException, ClassNotFoundException {
        this.address = address;
        this.port = port;
        int poolSize = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.isStopped = false;
        idToNameMap = initMap();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
            serverSocket.setSoTimeout(0);
            System.out.println("Server started!");
        } catch (IOException ex) {
            throw new RuntimeException("Unable to open ServerSocket: " + ex.getMessage());
        }

        Thread ServerSocketHandler = new Thread(() -> {
            while (!isStopped) {
                try {
                    System.out.println("Waiting for connections.....");
                    Socket client = serverSocket.accept();
                    executorService.execute(new Session(client));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        ServerSocketHandler.start();
    }

    public synchronized void stop() throws IOException {
        saveMap();
        System.out.println("Stopping server...");
        isStopped = true;
        if (serverSocket != null)
            serverSocket.close();
        executorService.shutdown();
    }

    private static ConcurrentHashMap<Long, String> initMap() throws IOException, ClassNotFoundException {
        return Files.exists(Path.of(FileDirectory.mapPath))
                ? loadMap()
                : new ConcurrentHashMap<>();
    }

    private static ConcurrentHashMap<Long, String> loadMap() throws IOException, ClassNotFoundException {
        return (ConcurrentHashMap<Long, String>) SerializationUtils.deserialize(FileDirectory.mapPath);
    }

    private static void saveMap() throws IOException {
        SerializationUtils.serialize(idToNameMap, FileDirectory.mapPath);
    }

    private class Session implements Runnable {

        private final Socket client;
        private final ObjectOutputStream output;
        private final ObjectInputStream input;
        private ClientRequest clientRequest;
        private final ServerResponse serverResponse;

        public Session(Socket client) throws IOException {
            this.client = client;
            this.output = new ObjectOutputStream(client.getOutputStream());
            this.input = new ObjectInputStream(client.getInputStream());
            this.serverResponse = new ServerResponse();
        }

        @Override
        public void run() {
            try {
                handleClientRequest();
                endSession();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


        private void handleClientRequest() throws IOException, ClassNotFoundException {

            setClientRequest();

            switch (clientRequest.getMethod()) {
                case "GET" -> handleGetRequest();
                case "PUT" -> handlePutRequest();
                case "DELETE" -> handleDeleteRequest();
                case "exit" -> handleExitRequest();
            }

        }

        private void handleGetRequest() throws IOException {
            String fileName = byNameOrById();
            if (fileName == null) {
                serverResponse.setCode(NOT_FOUND);
                sendResponse();
            } else {
                serverResponse.setCode(OK);
                serverResponse.setFileBytes(
                        FileUtils.getFileBytes(fileName)
                );
                sendResponse();
            }
        }

        private void handlePutRequest() throws IOException {
            long id = Math.abs(IdGenerator.nextId());

            if (FileUtils.exists(clientRequest.getNameOrId())) {
                serverResponse.setCode(FORBIDDEN);
                sendResponse();
            } else {
                FileUtils.saveFile(clientRequest.getNameOrId(), clientRequest.getFileContent());
                idToNameMap.put(id, clientRequest.getNameOrId());
                serverResponse.setId(id);
                serverResponse.setCode(OK);
                sendResponse();
            }

        }

        private void handleDeleteRequest() throws IOException {
            String fileName = byNameOrById();
            if (fileName == null) {
                serverResponse.setCode(NOT_FOUND);
                sendResponse();
            } else {
                FileUtils.deleteFile(fileName);
                serverResponse.setCode(OK);
                sendResponse();
            }
        }

        private void handleExitRequest() throws IOException {
            endSession();
            stop();
        }

        private String byNameOrById() {
            switch (clientRequest.getRequestBy()) {
                case "BY_NAME" -> {
                    return FileUtils.exists(clientRequest.getNameOrId())
                            ? clientRequest.getNameOrId() : null;
                }
                case "BY_ID" -> {
                    String fileName = idToNameMap.get(Long.valueOf(clientRequest.getNameOrId()));
                    if (fileName != null) {
                        return FileUtils.exists(fileName) ? fileName : null;
                    }
                }
            }
            return null;
        }

        private void endSession() throws IOException {
            this.input.close();
            this.output.close();
            this.client.close();
        }

        public void setClientRequest() throws IOException, ClassNotFoundException {
            this.clientRequest = getClientRequest();
        }

        private ClientRequest getClientRequest() throws IOException, ClassNotFoundException {
            return (ClientRequest) input.readObject();
        }

        private void sendResponse() throws IOException {
            output.writeObject(this.serverResponse);
        }


    }


}
