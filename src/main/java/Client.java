import java.io.*;
import java.net.*;

public class Client implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedReader bufferedReaderFromTerminal;
    private BufferedWriter bufferedWriter;
    private String username;
    private int numbers;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedReaderFromTerminal = new BufferedReader(new InputStreamReader(System.in));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.numbers = 0;
    }

    public void sendMessages() throws IOException {
        String messageToSend;
        while (socket.isConnected()) {
            messageToSend = bufferedReaderFromTerminal.readLine();
            if (!messageToSend.equals("close")) {
                numbers++;
                if (numbers == 1) {
                    username = messageToSend;
                }
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                close();
            }
        }
    }

    public void run() {
        try {
            while(socket.isConnected()) {
                System.out.println(bufferedReader.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        bufferedWriter.close();
        bufferedReader.close();
        bufferedReaderFromTerminal.close();
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 1004);
        Client client = new Client(socket);
        Thread listenForMessage = new Thread(client);
        listenForMessage.start();
        client.sendMessages();
    }
}
