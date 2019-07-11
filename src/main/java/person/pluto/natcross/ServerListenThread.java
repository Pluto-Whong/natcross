package person.pluto.natcross;

import java.io.IOException;
import java.io.OutputStream;
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
    private ServerSocket listenServerSocket;

    private Socket controlSocket;

    private Map<String, SocketPart> socketPartMap = new TreeMap<>();

    public ServerListenThread(Integer port, Integer controlPort) throws IOException {
        this.listenPort = port;
        listenServerSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                Socket listenSocket = listenServerSocket.accept();

                if (controlSocket == null) {
                    break;
                }

                String socketPartKey = CommonFormat.getSocketPartKey(listenPort);

                SocketPart socketPart = new SocketPart();
                socketPart.setSocketPartKey(socketPartKey);
                socketPart.setListenSocket(listenSocket);

                socketPartMap.put(socketPartKey, socketPart);

                sendClientWait(socketPartKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        this.isAlive = true;
        if (!this.isAlive()) {
            this.start();
        }
    }

    public void doSetPartClient(String socketPartKey, Socket sendSocket) {
        SocketPart socketPart = socketPartMap.get(socketPartKey);
        if (socketPart == null) {
            return;
        }
        socketPart.setSendSocket(sendSocket);
    }

    public void sendClientWait(String socketPartKey) {
        try {
            OutputStream outputStream = this.controlSocket.getOutputStream();
            outputStream.write(socketPartKey.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancell() {
        isAlive = false;

        if (listenServerSocket != null) {
            try {
                listenServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (controlSocket != null) {
            try {
                controlSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.controlSocket = null;
        }

        if (socketPartMap != null) {
            for (Entry<String, SocketPart> entry : socketPartMap.entrySet()) {
                SocketPart socketPart = entry.getValue();
                if (socketPart != null) {
                    socketPart.cancell();
                }
            }
            socketPartMap.clear();
        }
    }

    public Integer getListenPort() {
        return this.listenPort;
    }

    public void setControlSocket(Socket controlSocket) {
        if (this.controlSocket != null) {
            try {
                this.controlSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.controlSocket = controlSocket;
    }

}
