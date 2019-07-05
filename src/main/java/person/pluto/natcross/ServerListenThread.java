package person.pluto.natcross;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * <p>
 * 
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 10:53:33
 */
public class ServerListenThread extends Thread {

    private boolean isAlive = true;
    private Integer listenPort;
    private ServerSocket serverSocket;
    private ServerSocket controlSocket;

    private Map<String, SocketPart> socketPartMap = new TreeMap<>();

    public ServerListenThread(Integer port) throws IOException {
        this.listenPort = port;
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                Socket listenSocket = serverSocket.accept();

                if (controlSocket == null) {
                    break;
                }

                String socketPartKey = CommonFormat.getSocketPartKey(listenPort);

                SocketPart socketPart = new SocketPart();
                socketPart.setSocketPartKey(socketPartKey);
                socketPart.setListenSocket(listenSocket);

                socketPartMap.put(socketPartKey, socketPart);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancell() {
        isAlive = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socketPartMap != null) {
            for (Entry<String, SocketPart> entry : socketPartMap.entrySet()) {
                SocketPart socketPart = entry.getValue();
                if (socketPart != null) {
                    socketPart.cancell();
                }
            }
        }
    }

    public Integer getListenPort() {
        return this.listenPort;
    }

}
