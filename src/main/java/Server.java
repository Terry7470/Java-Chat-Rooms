import java.net.*;
import java.util.*;
import java.io.*;

//This class is made for start, create thread and close server.
public class Server implements Runnable {
    private ServerSocket serverSocket;
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    private boolean serverStart = true;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        while(serverStart) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException e) {
                System.out.println("fail to connect to the client");
            }
        }
    }

    public void tellCloseServer() {
        for(ClientHandler clientHandler : ClientHandler.clientHandlers) {
            try {
                if (clientHandler.socketIsConnected()){
                    clientHandler.sendMessages("Server is shutting down");
                }
            } catch (IOException e) {
                System.out.println(clientHandler.getUserName() + " has disconnected");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1004);
        Server server = new Server(serverSocket);
        System.out.println("The Server is running");
        Thread startServer = new Thread(server);
        startServer.start();
        while(true){
            if(bufferedReader.readLine().equals("!close")){
                System.out.println("The Server is shutting down");
                server.serverStart = false;
                server.tellCloseServer();
                for(ClientHandler clientHandler : ClientHandler.clientHandlers) {
                    clientHandler.close();
                }
                serverSocket.close();
                bufferedReader.close();
                break;
            }
        }
    }

}

class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private String clientUsername;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private boolean isRunning;

    public ClientHandler(Socket socket) throws IOException {
        this.isRunning =  true;
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        clientHandlers.add(this);
    }

    public void run() {
        try {
            bufferedWriter.write("Your username: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            if (isRunning) {
                this.clientUsername = bufferedReader.readLine();
            }
            if(clientUsername != null) {
                broadcastMessages("Server: " + clientUsername + " has joined the chat");
                System.out.println("A new member named " + clientUsername + " has joined this ChatRoom");
            }

            String messageFromClient;
            while (isRunning) {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    broadcastMessages(clientUsername + ": " + messageFromClient);
                }
            }
        } catch (IOException e) {
            try {
                socket.close();
                bufferedWriter.close();
                bufferedReader.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void broadcastMessages(String messageToSend) throws IOException {
        for(ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.socket.isConnected() && clientHandler.clientUsername != clientUsername && clientHandler.clientUsername != null) {
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }
        }
    }

    public void sendMessages(String messageToSend) throws IOException {
        bufferedWriter.write(messageToSend);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public void close() throws IOException {
        this.isRunning = false;
        socket.close();
        bufferedWriter.close();
        bufferedReader.close();
    }

    public String getUserName() {
        return clientUsername;
    }

    public boolean socketIsConnected() {
        return socket.isConnected();
    }
}
