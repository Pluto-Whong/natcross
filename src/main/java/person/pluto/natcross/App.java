package person.pluto.natcross;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class App {
    static int lisPort = 8899;

    static String sendHost = "127.0.0.1";
    static int sendPort = 9080;

    static Reader lisReader;
    static Writer lisWriter;

    static Reader sendReader;
    static Writer sendWriter;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(lisPort);
        
        
        Socket socket = server.accept();
        lisReader = new InputStreamReader(socket.getInputStream());
        lisWriter = new OutputStreamWriter(socket.getOutputStream());

        createClient();

        InputToOutputThread lisToSend = new InputToOutputThread(lisReader, sendWriter);
        InputToOutputThread sendToLis = new InputToOutputThread(sendReader, lisWriter);

        sendToLis.start();
        lisToSend.start();
        
    }

    public static void createClient() throws UnknownHostException, IOException {
        Socket client = new Socket(sendHost, sendPort);

        sendReader = new InputStreamReader(client.getInputStream());
        sendWriter = new OutputStreamWriter(client.getOutputStream());
    }

}
