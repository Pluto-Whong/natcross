package person.pluto.natcross.serveritem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONObject;

import person.pluto.natcross.common.InteractiveUtil;
import person.pluto.natcross.model.InteractiveModel;
import person.pluto.natcross.model.InteractiveTypeEnum;

/**
 * <p>
 * 监听服务
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 10:53:33
 */
public class ClientServiceThread extends Thread {

    private boolean isAlive = false;
    private Integer listenPort;
    private ServerSocket listenServerSocket;

    public ClientServiceThread(Integer port, Integer controlPort) throws IOException {
        this.listenPort = port;
        listenServerSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                Socket listenSocket = listenServerSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void procMethod(Socket listenSocket) {
        try {
            InputStream inputStream = listenSocket.getInputStream();
            InteractiveModel interactiveModel = InteractiveUtil.recv(inputStream);

            String interactiveType = interactiveModel.getInteractiveType();
            JSONObject jsonObject = interactiveModel.getData();

            InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(interactiveType);
            if (interactiveTypeEnum == null) {
                return;
            }
            if (interactiveTypeEnum.equals(InteractiveTypeEnum.CLIENT_CONTROL)) {
//                jsonObject.toJavaObject(clazz);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        this.isAlive = true;
        if (!this.isAlive()) {
            super.start();
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

    }

    public Integer getListenPort() {
        return this.listenPort;
    }

}
