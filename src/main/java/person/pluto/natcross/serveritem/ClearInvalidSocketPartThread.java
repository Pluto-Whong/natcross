package person.pluto.natcross.serveritem;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import person.pluto.natcross.common.NatcrossConstants;

/**
 * 
 * <p>
 * 清理无效端口
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-08-21 12:59:03
 */
@Slf4j
public class ClearInvalidSocketPartThread implements Runnable {

    private Thread myThread = null;

    private boolean isAlive = false;

    @Override
    public void run() {
        while (isAlive) {
            List<ServerListenThread> all = ListenServerControl.getAll();
            for (ServerListenThread thread : all) {
                thread.clearInvaildSocketPart();
            }

            try {
                TimeUnit.SECONDS.sleep(NatcrossConstants.SOCKET_PART_CLEAR_INTERVAL_SECONDS);
            } catch (InterruptedException e) {
                log.debug("ClearInvalidSocketPartThread sleep exception ", e);
            }
        }
    }

    public void start() {
        this.isAlive = true;
        if (myThread == null || !myThread.isAlive()) {
            myThread = new Thread(this);
            myThread.start();
        }
        log.info("ClearInvalidSocketPartThread started !");
    }

    public void cancell() {
        this.isAlive = false;
        if (myThread != null) {
            myThread.interrupt();
            myThread = null;
        }
        log.info("ClearInvalidSocketPartThread cancell !");
    }

}
