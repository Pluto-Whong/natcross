package person.pluto.natcross.clientitem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross.common.IBelongControl;
import person.pluto.natcross.common.InteractiveUtil;
import person.pluto.natcross.model.InteractiveModel;
import person.pluto.natcross.model.NatcrossResultModel;
import person.pluto.natcross.model.enumeration.InteractiveTypeEnum;
import person.pluto.natcross.model.enumeration.NatcrossResultEnum;
import person.pluto.natcross.model.interactive.ClientControlModel;
import person.pluto.natcross.model.interactive.ClientWaitModel;
import person.pluto.natcross.serveritem.SocketPart;

/**
 * <p>
 * 客户端控制服务
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 10:53:33
 */
@Slf4j
public class ClientControlThread implements Runnable, IBelongControl {

    private Thread myThread = null;

    private boolean isAlive = false;

    private String clientServiceIp;
    private Integer clientServicePort;
    private Integer listenServerPort;
    private String destIp;
    private Integer destPort;

    private Socket client;
    private OutputStream outputStream;
    private InputStream inputStream;

    private Map<String, SocketPart> socketPartMap = new TreeMap<>();

    private ClientHeartThread clientHeartThread;

    public ClientControlThread(String clientServiceIp, Integer clientServicePort, Integer listenServerPort,
            String destIp, Integer destPort) throws IOException {
        this.setClientServiceIpPort(clientServiceIp, clientServicePort);
        this.setListenServerPort(listenServerPort);
        this.setDestIpPort(destIp, destPort);
        clientHeartThread = new ClientHeartThread(this);
    }

    /**
     * 触发控制服务
     *
     * @author Pluto
     * @since 2019-07-18 19:02:15
     * @return
     * @throws UnknownHostException
     * @throws IOException
     */
    public boolean createControl() throws UnknownHostException, IOException {
        this.client = new Socket(this.clientServiceIp, this.clientServicePort);
        this.outputStream = client.getOutputStream();
        this.inputStream = client.getInputStream();

        InteractiveModel interactiveModel = InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONTROL,
                new ClientControlModel(this.listenServerPort));
        InteractiveUtil.send(this.outputStream, interactiveModel);

        InteractiveModel recv = InteractiveUtil.recv(inputStream);
        log.info(recv.toJSONString());

        NatcrossResultModel javaObject = recv.getData().toJavaObject(NatcrossResultModel.class);

        if (StringUtils.equals(NatcrossResultEnum.SUCCESS.getCode(), javaObject.getRetCod())) {
            this.start();
            return true;
        }
        stopClient();
        return false;
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                InteractiveModel recvInteractiveModel = InteractiveUtil.recv(this.inputStream);
                procMethod(recvInteractiveModel);
            } catch (Exception e) {
                log.warn("client control [{}] to server is exception,will stopClient", listenServerPort);
                this.stopClient();
            }
        }
    }

    /**
     * 接收处理方法
     *
     * @author Pluto
     * @since 2019-07-19 09:10:22
     * @param recvInteractiveModel
     * @throws IOException
     */
    public void procMethod(InteractiveModel recvInteractiveModel) throws IOException {
        log.info("接收到新的指令: {}", recvInteractiveModel.toJSONString());

        String interactiveType = recvInteractiveModel.getInteractiveType();
        JSONObject jsonObject = recvInteractiveModel.getData();

        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(interactiveType);
        if (interactiveTypeEnum == null) {
            return;
        }
        if (interactiveTypeEnum.equals(InteractiveTypeEnum.CLIENT_WAIT)) {
            ClientWaitModel clientWaitModel = jsonObject.toJavaObject(ClientWaitModel.class);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    clientConnect(clientWaitModel);
                }
            }).start();

            // edit 20190725 by pluto 不回复，这样就可以快速建立连接
//            boolean clientConnect = clientConnect(clientWaitModel);
//
//            InteractiveModel sendInteractiveModel = null;
//            if (clientConnect) {
//                sendInteractiveModel = InteractiveModel.of(recvInteractiveModel.getInteractiveSeq(),
//                        InteractiveTypeEnum.COMMON_REPLY, NatcrossResultModel.ofSuccess());
//            } else {
//                sendInteractiveModel = InteractiveModel.of(recvInteractiveModel.getInteractiveSeq(),
//                        InteractiveTypeEnum.COMMON_REPLY, NatcrossResultModel.ofFail());
//            }
//            InteractiveUtil.send(outputStream, sendInteractiveModel);

            return;
        }

        return;
    }

    /**
     * 建立连接
     *
     * @author Pluto
     * @since 2019-07-19 09:10:42
     * @param clientWaitModel
     */
    private boolean clientConnect(ClientWaitModel clientWaitModel) {
        Socket destSocket = null;
        try {
            destSocket = new Socket(this.destIp, this.destPort);
        } catch (IOException e) {
            log.error("向目标建立连接失败 {}:{}", this.destIp, this.destPort);
            return false;
        }

        Socket clientSocket = null;
        try {
            clientSocket = new Socket(this.clientServiceIp, this.clientServicePort);
            InteractiveModel model = InteractiveModel.of(InteractiveTypeEnum.CLIENT_CONNECT,
                    new ClientWaitModel(clientWaitModel.getSocketPartKey()));

            OutputStream outputStream2 = clientSocket.getOutputStream();
            InputStream inputStream2 = clientSocket.getInputStream();

            InteractiveUtil.send(outputStream2, model);

            InteractiveModel recv = InteractiveUtil.recv(inputStream2);
            log.info(recv.toJSONString());

            NatcrossResultModel javaObject = recv.getData().toJavaObject(NatcrossResultModel.class);

            if (!StringUtils.equals(NatcrossResultEnum.SUCCESS.getCode(), javaObject.getRetCod())) {
                throw new RuntimeException("绑定失败");
            }

        } catch (Exception e) {
            log.error("打通隧道发生异常 {}:{}<->{}:{} ;[]", this.clientServiceIp, this.clientServicePort, this.destIp,
                    this.destPort, e.getLocalizedMessage());
            try {
                destSocket.close();
            } catch (IOException e1) {
                log.debug("关闭目标端口异常", e);
            }

            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    log.debug("关闭客户端口异常", e);
                }
            }
            return false;
        }

        SocketPart socketPart = new SocketPart(this);
        socketPart.setSocketPartKey(clientWaitModel.getSocketPartKey());
        socketPart.setListenSocket(destSocket);
        socketPart.setSendSocket(clientSocket);
        boolean createPassWay = socketPart.createPassWay();
        if (!createPassWay) {
            socketPart.cancell();
            return false;
        }

        socketPartMap.put(clientWaitModel.getSocketPartKey(), socketPart);
        return socketPart.createPassWay();
    }

    /**
     * 停止某对连接
     */
    @Override
    public boolean stopSocketPart(String socketPartKey) {
        log.debug("stopSocketPart[{}]", socketPartKey);
        SocketPart socketPart = socketPartMap.get(socketPartKey);
        if (socketPart == null) {
            return false;
        }
        socketPart.cancell();
        socketPartMap.remove(socketPartKey);
        return true;
    }

    /**
     * 发送心跳
     *
     * @author Pluto
     * @since 2019-07-19 09:42:30
     * @throws IOException
     */
    public void sendUrgentData() throws IOException {
        // 无需判空，空指针异常也是异常
        this.client.sendUrgentData(0xFF);
    }

    public void start() {
        this.isAlive = true;
        if (myThread == null || !myThread.isAlive()) {
            myThread = new Thread(this);
            myThread.start();
        }
        clientHeartThread.start();
    }

    /**
     * 停止客户端监听
     *
     * @author Pluto
     * @since 2019-07-19 09:24:41
     */
    public void stopClient() {
        isAlive = false;
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                log.warn("cancell [{}] exception :{}", client.getInetAddress().toString(), e.getLocalizedMessage());
            }
            this.client = null;
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
                log.warn("cancell [{}] inputStream exception :{}", client.getInetAddress().toString(),
                        e.getLocalizedMessage());
            }
            this.inputStream = null;
        }
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                log.warn("cancell [{}] outputStream exception :{}", client.getInetAddress().toString(),
                        e.getLocalizedMessage());
            }
            this.outputStream = null;
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
     * @since 2019-07-19 09:19:43
     */
    public void cancell() {

        clientHeartThread.cancel();
        stopClient();

        Set<String> keySet = socketPartMap.keySet();
        String[] array = keySet.toArray(new String[keySet.size()]);

        for (String key : array) {
            stopSocketPart(key);
        }

    }

    public String getClientServiceIp() {
        return clientServiceIp;
    }

    public Integer getClientServicePort() {
        return clientServicePort;
    }

    public void setClientServiceIpPort(String clientServiceIp, Integer clientServicePort) {
        this.clientServiceIp = clientServiceIp;
        this.clientServicePort = clientServicePort;
    }

    public Integer getListenServerPort() {
        return listenServerPort;
    }

    public void setListenServerPort(Integer listenServerPort) {
        this.listenServerPort = listenServerPort;
    }

    public String getDestIp() {
        return destIp;
    }

    public Integer getDestPort() {
        return destPort;
    }

    public void setDestIpPort(String destIp, Integer destPort) {
        this.destIp = destIp;
        this.destPort = destPort;
    }

    public boolean isAlive() {
        if (isAlive) {
            return true;
        }
        if (clientHeartThread == null) {
            return false;
        }
        return clientHeartThread.isAlive();
    }

}
