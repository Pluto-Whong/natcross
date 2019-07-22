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
                log.debug("send urgent data to {}", clientControlThread.getListenServerPort());
                clientControlThread.sendUrgentData();
                failCount = 0;
            } catch (Exception e) {
                log.warn("{} 心跳异常，即将重新连接", clientControlThread.getListenServerPort());
                clientControlThread.stopClient();

                if (isAlive) {
                    this.failCount++;
                    try {
                        boolean createControl = clientControlThread.createControl();
                        if (createControl) {
                            clientControlThread.start();
                            log.info("重新建立连接 {} 成功，在第 {} 次", clientControlThread.getListenServerPort(), this.failCount);
                            continue;
                        }
                    } catch (IOException reClientException) {
                        log.warn("重新建立连接" + clientControlThread.getListenServerPort() + "失败第 " + this.failCount + " 次",
                                reClientException);
                    }

                    log.warn("重新建立连接" + clientControlThread.getListenServerPort() + "失败第 " + this.failCount + " 次");

                    if (failCount >= NatcrossConstants.TRY_RECLIENT_COUNT) {
                        log.error("尝试重新连接 {} 超过最大次数，尝试关闭客户端", clientControlThread.getListenServerPort());
                        this.cancel();
                        clientControlThread.cancell();
                        log.info("尝试重新连接 {} 超过最大次数，关闭客户端成功", clientControlThread.getListenServerPort());
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
        if (myThread != null) {
            myThread.interrupt();
            myThread = null;
        }
    }

}
