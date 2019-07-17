package person.pluto.natcross;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import person.pluto.natcross.clientitem.ClientControlThread;
import person.pluto.natcross.common.CommonConstants;

@SuppressWarnings("unused")
public class App {

    public static void main(String[] args) throws IOException {
        ClientControlThread clientControlThread = new ClientControlThread("129.28.193.163", 10010, 8080, "127.0.0.1",
                16000);
        clientControlThread.createControl();
        clientControlThread.start();
    }

}
