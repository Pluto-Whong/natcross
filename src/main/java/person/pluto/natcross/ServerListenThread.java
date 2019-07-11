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
 * 监听服务
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
                    listenSocket.close();
                    continue;
                }

                String socketPartKey = CommonFormat.getSocketPartKey(listenPort);

                SocketPart socketPart = new SocketPart();
                socketPart.setSocketPartKey(socketPartKey);
                socketPart.setListenSocket(listenSocket);

                if (!sendClientWait(socketPartKey)) {
                    socketPart.cancell();
                    continue;
                }
                socketPartMap.put(socketPartKey, socketPart);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        this.isAlive = true;
        if (!this.isAlive()) {
            super.start();
        }
    }

    /**
     * 停止指定的端口
     *
     * @author Pluto
     * @since 2019-07-11 16:33:10
     * @param socketPartKey
     * @return
     */
    public boolean stopSocketPart(String socketPartKey) {
        SocketPart socketPart = socketPartMap.remove(socketPartKey);
        if (socketPart == null) {
            return false;
        }
        return true;
    }

    /**
     * 将接受到的连接进行设置组合
     * 
     * @param socketPartKey
     * @param sendSocket
     * @return
     */
    public boolean doSetPartClient(String socketPartKey, Socket sendSocket) {
        SocketPart socketPart = socketPartMap.get(socketPartKey);
        if (socketPart == null) {
            return false;
        }
        socketPart.setSendSocket(sendSocket);

        return socketPart.createPassWay();
    }

    /**
     * 告知客户端，有新连接
     *
     * @author Pluto
     * @since 2019-07-11 15:45:14
     * @param socketPartKey
     */
    public boolean sendClientWait(String socketPartKey) {
        try {
            OutputStream outputStream = this.controlSocket.getOutputStream();
            outputStream.write(socketPartKey.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            if (this.controlSocket == null || !this.controlSocket.isConnected() || !this.controlSocket.isClosed()) {
                // 保证control为置空状态
                stopListen();
            }
            return false;
        }
        return true;
    }

    public void stopListen() {
        isAlive = false;

        if (controlSocket != null) {
            try {
                controlSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.controlSocket = null;
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
