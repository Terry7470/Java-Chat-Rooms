import java.net.*;
import java.util.*;
import java.io.*;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() throws IOException {
        while(!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(socket);
            Thread thread = new Thread(clientHandler);

        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1004);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}

class ClientHandler {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private String clientUsername;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.clientUsername = bufferedReader.readLine();
        clientHandlers.add(this);
    }

    public void run() throws IOException {
        messageFromClient = bufferedReader.readLine();
        BroadcastMessages(messageFromClient);
    }

    public void BroadcastMessages(String messageToSend) {

    }

}
