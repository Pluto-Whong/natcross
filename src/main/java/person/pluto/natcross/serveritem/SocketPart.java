package person.pluto.natcross.serveritem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross.common.IBelongControl;
import person.pluto.natcross.common.InputToOutputThread;
import person.pluto.natcross.common.NatcrossConstants;

/**
 * 
 * <p>
 * socket匹配对
 * </p>
 *
 * @author Pluto
 * @since 2019-07-12 08:36:30
 */
@Data
@Slf4j
public class SocketPart implements IBelongControl {

    private boolean isAlive = false;
    private LocalDateTime createTime;

    private String socketPartKey;
    @Getter(lombok.AccessLevel.PRIVATE)
    private Socket listenSocket;
    @Getter(lombok.AccessLevel.PRIVATE)
    private Socket sendSocket;

    private InputToOutputThread serverToClientThread;
    private InputToOutputThread clientToServerThread;

    /**
     * 所属监听类
     */
    @Getter(lombok.AccessLevel.PRIVATE)
    private IBelongControl belongThread;

    public SocketPart(IBelongControl belongThread) {
        this.belongThread = belongThread;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 判断是否失效
     *
     * @author wangmin1994@qq.com
     * @since 2019-08-21 12:48:29
     * @return
     */
    public boolean isValid() {
        if (isAlive) {
            if (serverToClientThread == null || !serverToClientThread.isAlive() || clientToServerThread == null
                    || !clientToServerThread.isAlive()) {
                return false;
            }
            return isAlive;
        }

        long millis = Duration.between(createTime, LocalDateTime.now()).toMillis();
        return millis < NatcrossConstants.SOCKET_PART_INVAILD_MILLIS;
    }

    /**
     * 停止，并告知上层处理掉
     *
     * @author Pluto
     * @since 2019-07-11 17:04:52
     */
    public void stop() {
        this.cancell();
        if (belongThread != null) {
            belongThread.stopSocketPart(socketPartKey);
        }
        belongThread = null;
    }

    /**
     * 退出
     *
     * @author Pluto
     * @since 2019-07-11 17:04:39
     */
    public void cancell() {
        log.debug("socketPartKey {} will cancell", this.socketPartKey);
        this.isAlive = false;
        if (listenSocket != null) {
            try {
                listenSocket.close();
            } catch (IOException e) {
                log.debug("socketPart [{}] 监听端口 关闭异常", socketPartKey);
            }
            listenSocket = null;
        }

        if (sendSocket != null) {
            try {
                sendSocket.close();
            } catch (IOException e) {
                log.debug("socketPart [{}] 发送端口 关闭异常", socketPartKey);
            }
            sendSocket = null;
        }

        if (serverToClientThread != null) {
            serverToClientThread.cancell();
            serverToClientThread = null;
        }
        if (clientToServerThread != null) {
            clientToServerThread.cancell();
            clientToServerThread = null;
        }
    }

    /**
     * 建立隧道
     *
     * @author Pluto
     * @since 2019-07-11 16:36:08
     * @return
     */
    public boolean createPassWay() {
        if (this.isAlive) {
            return true;
        }
        this.isAlive = true;
        try {
            InputStream listInputStream = listenSocket.getInputStream();
            OutputStream lisOutputStream = listenSocket.getOutputStream();

            InputStream sendInputStream = sendSocket.getInputStream();
            OutputStream sendOutputStream = sendSocket.getOutputStream();

            serverToClientThread = new InputToOutputThread(listInputStream, sendOutputStream, this);
            clientToServerThread = new InputToOutputThread(sendInputStream, lisOutputStream, this);

            serverToClientThread.start();
            clientToServerThread.start();
        } catch (Exception e) {
            log.error("socketPart [" + this.socketPartKey + "] 隧道建立异常", e);
            this.stop();
            return false;
        }
        return true;
    }

    /**
     * 上次接收到关闭要求
     */
    @Override
    public void noticeStop() {
        this.stop();
    }

    public String getListenSocketString() {
        if (listenSocket == null) {
            return null;
        }

        return listenSocket.toString();
    }

    public String getSendSocketString() {
        if (sendSocket == null) {
            return null;
        }

        return sendSocket.toString();
    }

}
