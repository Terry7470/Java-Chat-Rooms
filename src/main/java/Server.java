import java.net.ServerSocket;
import java.net.Socket;
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
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1004);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}
