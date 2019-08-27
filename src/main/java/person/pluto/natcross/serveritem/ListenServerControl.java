package person.pluto.natcross.serveritem;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 转发监听服务控制类
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 11:25:44
 */
@Slf4j
public class ListenServerControl {

    private static Map<Integer, ServerListenThread> serverListenMap = new HashMap<>();

    /**
     * 加入新的监听服务进程
     *
     * @author Pluto
     * @since 2019-07-18 18:36:25
     * @param serverListen
     * @return
     */
    public static boolean add(ServerListenThread serverListen) {
        if (serverListen == null) {
            return false;
        }

        Integer listenPort = serverListen.getListenPort();
        ServerListenThread serverListenThread = serverListenMap.get(listenPort);
        if (serverListenThread != null) {
            // 必须要先remove掉才能add，讲道理如果原先的存在应该直接报错才对，也就是参数为null，所以这里不自动remove
            return false;
        }

        serverListenMap.put(listenPort, serverListen);
        return true;
    }

    /**
     * 去除指定端口的监听服务端口
     *
     * @author Pluto
     * @since 2019-07-18 18:36:35
     * @param listenPort
     * @return
     */
    public static boolean remove(Integer listenPort) {
        ServerListenThread serverListenThread = serverListenMap.get(listenPort);
        if (serverListenThread == null) {
            return true;
        }

        serverListenThread.cancell();
        serverListenMap.remove(listenPort);

        return true;
    }

    /**
     * 根据端口获取监听服务端口
     *
     * @author Pluto
     * @since 2019-07-18 18:36:52
     * @param listenPort
     * @return
     */
    public static ServerListenThread get(Integer listenPort) {
        return serverListenMap.get(listenPort);
    }

    /**
     * 获取全部监听服务
     *
     * @author Pluto
     * @since 2019-07-19 15:31:55
     * @return
     */
    public static List<ServerListenThread> getAll() {
        List<ServerListenThread> list = new LinkedList<>();
        serverListenMap.forEach((key, value) -> {
            list.add(value);
        });
        return list;
    }

    /**
     * 关闭所有监听服务
     *
     * @author Pluto
     * @since 2019-07-18 19:00:54
     */
    public static void closeAll() {
        Set<Integer> keySet = serverListenMap.keySet();
        Integer[] array = keySet.toArray(new Integer[keySet.size()]);
        for (Integer key : array) {
            remove(key);
        }
    }

    /**
     * 创建新的监听服务
     *
     * @author Pluto
     * @since 2019-07-19 13:59:24
     * @param port
     * @return
     */
    public static ServerListenThread createNewListenServer(Integer port) {
        ServerListenThread serverListenThread = null;
        try {
            serverListenThread = new ServerListenThread(port);
        } catch (IOException e) {
            log.warn("create listen server [{}] faild", port);
            return null;
        }
        // 若没有报错则说明没有监听该端口的线程，即不可正常使用原有端口，所以先进行强行remove，再进行add
        ListenServerControl.remove(port);
        ListenServerControl.add(serverListenThread);
        return serverListenThread;
    }

}
