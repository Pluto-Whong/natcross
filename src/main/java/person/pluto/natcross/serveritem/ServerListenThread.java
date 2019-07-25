package person.pluto.natcross.serveritem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import person.pluto.natcross.common.CommonFormat;
import person.pluto.natcross.common.IBelongControl;

import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 监听转发服务进程
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 10:53:33
 */
@Slf4j
public class ServerListenThread implements Runnable, IBelongControl {

    private Thread myThread = null;

    private boolean isAlive = false;
    private Integer listenPort;
    private ServerSocket listenServerSocket;

    private ControlSocket controlSocket;

    private Map<String, SocketPart> socketPartMap = new TreeMap<>();

    public ServerListenThread(Integer port) throws IOException {
        this.listenPort = port;
        listenServerSocket = new ServerSocket(port);
        log.info("server listen port[{}] is created!", this.listenPort);
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                Socket listenSocket = listenServerSocket.accept();

                // 如果没有控制接收socket，则取消接入，不主动关闭所有接口，防止controlSocket临时掉线，讲道理没有controlSocket也不会启动
                if (controlSocket == null) {
                    listenSocket.close();
                    continue;
                }

                String socketPartKey = CommonFormat.getSocketPartKey(listenPort);

                SocketPart socketPart = new SocketPart(this);
                socketPart.setSocketPartKey(socketPartKey);
                socketPart.setListenSocket(listenSocket);

                socketPartMap.put(socketPartKey, socketPart);
                // 发送指令失败，同controlSocket为空
                if (!sendClientWait(socketPartKey)) {
                    socketPartMap.remove(socketPartKey);
                    socketPart.cancell();
                    continue;
                }
            } catch (Exception e) {
                log.warn("监听服务[" + this.listenPort + "]发送通知服务异常", e);
                stopListen();
            }
        }
    }

    /**
     * 启动
     */
    public void start() {
        this.isAlive = true;
        if (myThread == null || !myThread.isAlive()) {
            myThread = new Thread(this);
            myThread.start();
        }
        log.info("server listen port[{}] is started!", this.listenPort);
    }

    /**
     * 停止指定的端口
     *
     * @author Pluto
     * @since 2019-07-11 16:33:10
     * @param socketPartKey
     * @return
     */
    @Override
    public boolean stopSocketPart(String socketPartKey) {
        log.debug("停止接口 stopSocketPart[{}]", socketPartKey);
        SocketPart socketPart = socketPartMap.remove(socketPartKey);
        if (socketPart == null) {
            return false;
        }
        socketPart.cancell();
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
        log.debug("接入接口 doSetPartClient[{}]", socketPartKey);
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
        log.info("告知新连接 sendClientWait[{}]", socketPartKey);
        boolean sendClientWait = false;

        try {
            sendClientWait = this.controlSocket.sendClientWait(socketPartKey);
        } catch (Exception e) {
            log.error("告知新连接 sendClientWait[" + socketPartKey + "] 发生未知异常", e);
            sendClientWait = false;
        }

        if (!sendClientWait) {
            log.warn("告知新连接 sendClientWait[" + socketPartKey + "] 失败");
            if (this.controlSocket == null || !this.controlSocket.isValid()) {
                // 保证control为置空状态
                stopListen();
            }
            return false;
        }
        return true;
    }

    /**
     * 关停监听服务，不注销已经建立的，并置空controlSocket
     *
     * @author Pluto
     * @since 2019-07-18 18:43:43
     */
    public void stopListen() {
        log.info("stopListen[{}]", this.listenPort);
        isAlive = false;

        if (controlSocket != null) {
            try {
                controlSocket.close();
            } catch (Exception e) {
                log.debug("监听服务控制端口关闭异常", e);
            }
            this.controlSocket = null;
        }

        if (myThread != null) {
            myThread.interrupt();
            myThread = null;
        }
    }

    /**
     * 退出
     *
     * @author Pluto
     * @since 2019-07-18 18:44:11
     */
    public void cancell() {
        log.debug("cancell[{}]", this.listenPort);

        this.stopListen();

        if (listenServerSocket != null) {
            try {
                listenServerSocket.close();
            } catch (IOException e) {
                log.debug("监听服务端口关闭异常", e);
            }
        }

        Set<String> keySet = socketPartMap.keySet();
        String[] array = keySet.toArray(new String[keySet.size()]);

        for (String key : array) {
            stopSocketPart(key);
        }

    }

    /**
     * 获取监听端口
     *
     * @author Pluto
     * @since 2019-07-18 18:45:57
     * @return
     */
    public Integer getListenPort() {
        return this.listenPort;
    }

    /**
     * 设置控制端口
     *
     * @author Pluto
     * @since 2019-07-18 18:46:05
     * @param controlSocket
     */
    public void setControlSocket(Socket controlSocket) {
        log.info("setControlSocket[{}]", this.listenPort);
        if (this.controlSocket != null) {
            try {
                this.controlSocket.close();
            } catch (Exception e) {
                log.debug("监听服务控制端口关闭异常", e);
            }
        }

        this.controlSocket = new ControlSocket(controlSocket);
        this.start();
    }

    /**
     * 获取已建立的连接
     *
     * @author Pluto
     * @since 2019-07-19 16:32:09
     * @return
     */
    public List<String> getSocketPartList() {
        Set<String> keySet = this.socketPartMap.keySet();
        return Arrays.asList(keySet.toArray(new String[keySet.size()]));
    }

    public Boolean isAlive() {
        return isAlive;
    }

}
