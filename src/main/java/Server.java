import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.time.*;

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
                if (serverStart){
                    System.out.println("fail to connect to the client");
                }
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
    private static BufferedReader readTxt;
    private BufferedWriter bufferedWriter;
    private static BufferedWriter intoTxt;

    static {
        try {
            intoTxt = new BufferedWriter(new FileWriter("/Users/TerryLi/Desktop/messagesRecord"));
            readTxt = new BufferedReader(new FileReader("/Users/TerryLi/Desktop/messagesRecord"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRunning;
    private int theNumberORM;


    public ClientHandler(Socket socket) throws IOException {
        this.isRunning =  true;
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        clientHandlers.add(this);
        this.theNumberORM = 0;
    }

    public void run() {
        try {
            bufferedWriter.write("Your username: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            this.clientUsername = bufferedReader.readLine();
            if(clientUsername != null) {
                broadcastMessages("Server: " + clientUsername + " has entered the chat      " + getTime());
                System.out.println("A new member named " + clientUsername + " has joined this ChatRoom");
            }

            String messageFromClient;
            while (isRunning) {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null || messageFromClient.isEmpty() || messageFromClient.isBlank()) continue;
                if (messageFromClient.length() < 9) {
                    theNumberORM++;
                    broadcastMessages(clientUsername + ": " + messageFromClient + "      " +  getTime() + "  " + theNumberORM + " message(s)");
                    writeRecord(clientUsername + ": " + messageFromClient + "      " +  getTime() + "  " + theNumberORM + " message(s)");
                    continue;
                }
                if(messageFromClient.substring(0, 8).equals("#search ")) {
                    String keyword = messageFromClient.substring(8, messageFromClient.length());
                    String line;
                    String keyOfLine;
                    boolean haveKeyword = false;
                    while(true) {
                        line = readTxt.readLine();
                        if (line == null) break;
                        keyOfLine = line.substring(9, line.length()-33);
                        if (keyOfLine.contains(keyword)){
                            bufferedWriter.write(line);
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            haveKeyword = true;
                        }
                    }
                    if (!haveKeyword) {
                        bufferedWriter.write("No relevant statements");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    readTxt.close();
                    readTxt = new BufferedReader(new FileReader("/Users/TerryLi/Desktop/messagesRecord"));
                    continue;
                }
                theNumberORM++;
                broadcastMessages(clientUsername + ": " + messageFromClient + "      " +  getTime() + "  " + theNumberORM + " message(s)");
                writeRecord(clientUsername + ": " + messageFromClient + "      " +  getTime() + "  " + theNumberORM + " message(s)");
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

    public void writeRecord(String messageToSend) throws IOException {
        intoTxt.write("History: " + messageToSend);
        intoTxt.newLine();
        intoTxt.flush();
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
        intoTxt.close();
        readTxt.close();
    }

    public String getUserName() {
        return clientUsername;
    }

    public boolean socketIsConnected() {
        return socket.isConnected();
    }

    private String getTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        String theTime = currentTime.format(dateTimeFormatter);
        return theTime;
    }

}
