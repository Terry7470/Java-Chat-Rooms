import java.io.*;
import java.net.*;

public class Client implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedReader bufferedReaderFromTerminal;
    private BufferedWriter bufferedWriter;
    private String username;
    private int numbers;
    private boolean clientIsOpen;

    public Client(Socket socket) throws IOException {
        this.clientIsOpen = true;
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedReaderFromTerminal = new BufferedReader(new InputStreamReader(System.in));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.numbers = -1;
    }

    public void sendMessages() throws IOException {
        String messageToSend;
        while (clientIsOpen) {
            messageToSend = bufferedReaderFromTerminal.readLine();
            if (!messageToSend.equals("close")) {
                numbers++;
                if (numbers == 0) {
                    username = messageToSend;
                }
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                clientIsOpen = false;
                close();
                break;
            }
        }
    }

    public void run() {
        try {
            while(clientIsOpen) {
                System.out.println(bufferedReader.readLine());
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

    public void close() throws IOException {
        bufferedReaderFromTerminal.close();
        bufferedWriter.close();
        bufferedReader.close();
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
