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
import person.pluto.natcross.common.NatcrossConstants;
import person.pluto.natcross.serveritem.ClearInvalidSocketPartThread;
import person.pluto.natcross.serveritem.ClientServiceThread;
import person.pluto.natcross.serveritem.ListenServerControl;
import person.pluto.natcross.serveritem.ServerListenThread;

@SuppressWarnings("unused")
public class ServerApp {

    public static void main(String[] args) throws IOException {
        // 客户端连接
        ClientServiceThread clientServiceThread = new ClientServiceThread(Integer.valueOf(args[0]));
        clientServiceThread.start();

        for (int i = 1; i < args.length; i++) {
            // 监听端口
            ListenServerControl.createNewListenServer(Integer.valueOf(args[i]));
        }

        ClearInvalidSocketPartThread clearInvalidSocketPartThread = new ClearInvalidSocketPartThread();
        clearInvalidSocketPartThread.start();
    }

}
