package person.pluto.natcross.clientitem;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross.common.NatcrossConstants;

@Slf4j
public class ClientHeartThread implements Runnable {

    private Thread myThread = null;

    private boolean isAlive = false;

    private ClientControlThread clientControlThread;

    private Integer failCount = 0;

    public ClientHeartThread(ClientControlThread clientControlThread) {
        this.clientControlThread = clientControlThread;
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                Thread.sleep(NatcrossConstants.CLIENT_HEART_INTERVAL);
            } catch (InterruptedException e) {
                return;
            }
            try {
                log.debug("send urgent data");
                clientControlThread.sendUrgentData();
                failCount = 0;
            } catch (Exception e) {
                log.warn("心跳异常，即将退出", e);
                clientControlThread.stopClient();

                if (isAlive) {
                    try {
                        boolean createControl = clientControlThread.createControl();
                        if (createControl) {
                            clientControlThread.start();
                            continue;
                        }
                    } catch (IOException reClientException) {
                        this.failCount++;
                        log.warn("重新建立连接失败第 " + this.failCount + " 次", reClientException);
                    }

                    if (failCount >= NatcrossConstants.TRY_RECLIENT_COUNT) {
                        log.error("尝试重新连接超过最大次数，尝试关闭客户端");
                        this.cancel();
                        clientControlThread.cancell();
                        log.info("尝试重新连接超过最大次数，关闭客户端成功");
                    }
                }
            }
        }
    }

    public void start() {
        this.isAlive = true;
        if (myThread == null || !myThread.isAlive()) {
            myThread = new Thread(this);
            myThread.start();
        }
    }

    public void cancel() {
        this.isAlive = false;
        myThread.interrupt();
    }

}
