package person.pluto.natcross.serveritem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import person.pluto.natcross.common.InteractiveUtil;
import person.pluto.natcross.model.InteractiveModel;
import person.pluto.natcross.model.enumeration.InteractiveTypeEnum;
import person.pluto.natcross.model.interactive.ClientWaitModel;

/**
 * 
 * <p>
 * 控制socket实例
 * </p>
 *
 * @author Pluto
 * @since 2019-07-17 11:03:56
 */
//@Slf4j
public class ControlSocket {

    private Socket controlSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    /**
     * 锁定输出资源标志
     */
    private Lock socketLock = new ReentrantLock();

    public ControlSocket(Socket socket) {
        this.controlSocket = socket;
    }

    /**
     * 是否有效
     *
     * @author Pluto
     * @since 2019-07-18 18:33:47
     * @return
     */
    public boolean isValid() {
        if (this.controlSocket == null || !this.controlSocket.isConnected() || !this.controlSocket.isClosed()) {
            return false;
        }
        return true;
    }

    /**
     * 关闭
     *
     * @author Pluto
     * @since 2019-07-18 18:33:54
     * @throws IOException
     */
    public void close() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                // no thing
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                // no thing
            }
        }

        if (controlSocket != null) {
            try {
                controlSocket.close();
            } catch (Exception e) {
                // no thing
            }
        }

        socketLock.unlock();
    }

    /**
     * 获取输出接口
     *
     * @author Pluto
     * @since 2019-07-18 18:34:22
     * @return
     * @throws IOException
     */
    private OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = this.controlSocket.getOutputStream();
        }
        return outputStream;
    }

    /**
     * 获取输入流
     *
     * @author Pluto
     * @since 2019-07-25 15:51:03
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = this.controlSocket.getInputStream();
        }
        return inputStream;
    }

    /**
     * 发送新接入接口
     *
     * @author Pluto
     * @since 2019-07-18 18:34:38
     * @param socketPartKey
     * @throws IOException
     */
    public boolean sendClientWait(String socketPartKey) {
        InteractiveModel model = InteractiveModel.of(InteractiveTypeEnum.CLIENT_WAIT,
                new ClientWaitModel(socketPartKey));

        socketLock.lock();
        try {
            InteractiveUtil.send(getOutputStream(), model);
//            InteractiveModel recv = InteractiveUtil.recv(getInputStream());
//            log.info("发送等待连接通知后收到 {}", recv.toJSONString());
//
//            NatcrossResultModel javaObject = recv.getData().toJavaObject(NatcrossResultModel.class);
//
//            if (!StringUtils.equals(NatcrossResultEnum.SUCCESS.getCode(), javaObject.getRetCod())) {
//                throw new RuntimeException("客户端建立连接失败");
//            }
        } catch (Exception e) {
            return false;
        } finally {
            socketLock.unlock();
        }

        return true;
    }

}
