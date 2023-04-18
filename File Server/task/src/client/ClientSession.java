package client;

import client.utils.UserPrompts;
import shared.requests.ClientRequest;
import shared.responses.ServerResponse;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class ClientSession {
    private final static String CLIENT_DIRECTORY = System.getProperty("user.dir") +
            File.separator + "src" + File.separator + "client" + File.separator + "data" + File.separator;
    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;
    private final Scanner scanner;
    private ClientRequest clientRequest;
    private static final Map<String, String> inputActionMap = Map.of(
            "1", "GET",
            "2", "PUT",
            "3", "DELETE"
    );

    private static final Map<Integer, String> byNameOrIdMap = Map.of(
            1, "BY_NAME",
            2, "BY_ID"
    );

    private static final Runnable requestSent = () -> System.out.println(UserPrompts.REQUEST_SENT);

    public ClientSession(String address, int port) throws IOException {
        this.scanner = new Scanner(System.in);
        this.socket = new Socket(InetAddress.getByName(address), port);
        this.input = new ObjectInputStream(socket.getInputStream());
        this.output = new ObjectOutputStream(socket.getOutputStream());

    }

    public void start() throws IOException, ClassNotFoundException {
        System.out.println("Client started!");
        handleClientRequest();
        end();
    }

    public void end() throws IOException {
        this.output.close();
        this.input.close();
        this.socket.close();
    }

    private String getUserInputString(String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    private String getUserInputByNameOrId(String prompt) {
        System.out.println(prompt);
        int input = scanner.nextInt();
        scanner.nextLine();
        return byNameOrIdMap.get(input);
    }

    public void setClientRequest(String requestInput) {
        if (requestInput.equals("exit")) {
            this.clientRequest = new ClientRequest(requestInput);
        } else {
            this.clientRequest = new ClientRequest(inputActionMap.get(requestInput));
        }
    }

    private void handleClientRequest() throws IOException, ClassNotFoundException {
        String requestInput = getUserInputString(UserPrompts.CHOSE_ACTION);
        setClientRequest(requestInput);
        switch (clientRequest.getMethod()) {
            case "GET" -> executeGetRequest();
            case "PUT" -> executePutRequest();
            case "DELETE" -> executeDeleteRequest();
            case "exit" -> executeExitRequest();
        }
    }

    private void putRequestFilename(String fileToSend) {
        String newFileName = getUserInputString(UserPrompts.NAME_FOR_SAVED_FILE);
        if (newFileName.equals("")) {
            String fileExtension = fileToSend.split("\\.")[1];
            String randomName = UUID.randomUUID().toString() + "." + fileExtension;
            this.clientRequest.setNameOrId(randomName);
        } else {
            this.clientRequest.setNameOrId(newFileName);
        }
    }

    private void promptUserNameOrId() {
        switch (clientRequest.getRequestBy()) {
            case "BY_NAME" -> clientRequest.setNameOrId(getUserInputString(UserPrompts.ENTER_FILENAME));
            case "BY_ID" -> clientRequest.setNameOrId(getUserInputString(UserPrompts.ENTER_ID));
        }
    }

    private void executeGetRequest() throws IOException, ClassNotFoundException {
        this.clientRequest.setRequestBy(getUserInputByNameOrId(UserPrompts.GET_BY_PROMPT));
        // will have to check if it is by id or name at the server
        promptUserNameOrId();
        sendRequest();
        ServerResponse serverResponse = receiveResponse();
        if (serverResponse.getCode() == 404) {
            System.out.println(UserPrompts.FILE_NOT_FOUND);
        } else {
            saveFile(serverResponse);
            System.out.println(UserPrompts.FILE_SAVED);
        }
    }

    private void executePutRequest() throws IOException, ClassNotFoundException {
        String fileToSend = getUserInputString(UserPrompts.ENTER_FILENAME);
        File file = new File(CLIENT_DIRECTORY + fileToSend);

        if (file.exists()) {
            putRequestFilename(fileToSend);
            setFileContent(file);
            sendRequest();

            ServerResponse serverResponse = receiveResponse();

            System.out.println(
                    serverResponse.getCode() == 403
                            ? UserPrompts.SAVING_FILE_NOT_OK
                            : UserPrompts.SAVING_FILE_OK + serverResponse.getId()
            );

        } else {
            System.out.println(fileToSend + " does not exits");
        }
    }

    private void executeDeleteRequest() throws IOException, ClassNotFoundException {
        this.clientRequest.setRequestBy(getUserInputByNameOrId(UserPrompts.DELETE_BY_PROMPT));
        promptUserNameOrId();
        sendRequest();
        ServerResponse serverResponse = receiveResponse();
        System.out.println(serverResponse.getCode() == 404 ? UserPrompts.FILE_NOT_FOUND : UserPrompts.DELETE_OK);
    }

    private void executeExitRequest() throws IOException {
        sendRequest();
    }

    private void setFileContent(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            this.clientRequest.setFileContent(
                    fileInputStream.readAllBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveFile(ServerResponse serverResponse) {
        String savedFileName = getUserInputString(UserPrompts.NAME_FOR_DOWNLOADED_FILE);
        Path path = Path.of(CLIENT_DIRECTORY + savedFileName);
        if (Files.notExists(path)) {
            try {
                Files.createFile(path);
                Files.write(path, serverResponse.getFileBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(savedFileName + "already exists!");
        }
    }

    private void sendRequest() throws IOException {
        output.writeObject(this.clientRequest);
        requestSent.run();
    }

    private ServerResponse receiveResponse() throws IOException, ClassNotFoundException {
        return (ServerResponse) input.readObject();
    }

}
