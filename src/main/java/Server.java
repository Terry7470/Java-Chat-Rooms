import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.time.*;

//This class is made for start, create thread and close server.
public class Server implements Runnable {
    private ServerSocket serverSocket;
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    //This is a variable describing the state of the server being open.
    private boolean serverStart = true;
    //The constructor of the Server class.
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    //The server thread responsible for spawning new Client Handler threads.
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
    //A function for indicating that the client-server is in the process of shutting down.
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
        //Accepts socket requests on the default IP and port 1004.
        ServerSocket serverSocket = new ServerSocket(1004);
        Server server = new Server(serverSocket);
        System.out.println("The Server is running");
        //Runs the content of the `run` function as a separate thread.
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
//A class used to control the client.
class ClientHandler implements Runnable {
    //A dynamic array used to store client handlers.
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private String clientUsername;
    private BufferedReader bufferedReader;
    //Read content from a local file.
    private static BufferedReader readTxt;
    private BufferedWriter bufferedWriter;
    //Write the chat records to a local file.
    private static BufferedWriter intoTxt;
    //Specify the local file.
    static {
        try {
            intoTxt = new BufferedWriter(new FileWriter("/Users/TerryLi/Desktop/messagesRecord"));
            readTxt = new BufferedReader(new FileReader("/Users/TerryLi/Desktop/messagesRecord"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //Variable indicating whether the client handler is running.
    private boolean isRunning;
    private int theNumberORM;

//    Constructor: includes reading, sending, and counting.
    public ClientHandler(Socket socket) throws IOException {
        this.isRunning =  true;
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        clientHandlers.add(this);
        this.theNumberORM = 0;
    }
    //Independent thread section, including processing content received from clients.
    public void run() {
        try {
            bufferedWriter.write("Your username: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            this.clientUsername = bufferedReader.readLine();
            //Ensure the validity of the content.
            if(clientUsername != null) {
                broadcastMessages("Server: " + clientUsername + " has entered the chat      " + getTime());
                System.out.println("A new member named " + clientUsername + " has joined this ChatRoom");
            }
            //Process different keywords separately.
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
                    //Reset the file read pointer to the beginning.
                    readTxt.close();
                    readTxt = new BufferedReader(new FileReader("/Users/TerryLi/Desktop/messagesRecord"));
                    continue;
                }
                theNumberORM++;
                //Broadcast the received chat messages.
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
    //Function to broadcast messages, excluding the sender.
    public void broadcastMessages(String messageToSend) throws IOException {
        for(ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.socket.isConnected() && clientHandler.clientUsername != clientUsername && clientHandler.clientUsername != null) {
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }
        }
    }
    //Function to record messages locally.
    public void writeRecord(String messageToSend) throws IOException {
        intoTxt.write("History: " + messageToSend);
        intoTxt.newLine();
        intoTxt.flush();
    }
    //Function to send messages to the current instance.
    public void sendMessages(String messageToSend) throws IOException {
        bufferedWriter.write(messageToSend);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
    //Function to close the server.
    public void close() throws IOException {
        this.isRunning = false;
        socket.close();
        bufferedWriter.close();
        bufferedReader.close();
        intoTxt.close();
        readTxt.close();
    }
    //Function to retrieve the username corresponding to the instance's client.
    public String getUserName() {
        return clientUsername;
    }
    //Code to determine if the client is still connected.
    public boolean socketIsConnected() {
        return socket.isConnected();
    }
    //A function to get the current time.
    private String getTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        String theTime = currentTime.format(dateTimeFormatter);
        return theTime;
    }

}
