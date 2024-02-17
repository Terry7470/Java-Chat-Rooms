import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.username = username;
    }

    public void sendMessages() {

    }

    public void listenformessages() {

    }

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 1004);
        Client client = new Client(socket, args[0]);
        client.listenformessages();
        client.sendMessages();
    }
}
