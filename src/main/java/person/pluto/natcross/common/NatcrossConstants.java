package person.pluto.natcross.common;

public class NatcrossConstants {

    /**
     * 数据发送缓存字节
     */
    public static Integer STREAM_CACHE_SIZE = 1024;
    /**
     * 服务端地址
     */
    public static String CLIENT_SERVER_IP = "127.0.0.1";
    /**
     * 客户端监听端口
     */
    public static Integer CLIENT_SERVER_PORT = 10010;
    /**
     * 客户端心跳测试间隔
     */
    public static Long CLIENT_HEART_INTERVAL = 10L * 1000L;
    /**
     * 客户端心跳测试，最大次数
     */
    public static Integer TRY_RECLIENT_COUNT = 10;
    /**
     * socketPart有效时间，若超过则关闭
     */
    public static Long SOCKET_PART_INVAILD_MILLIS = 60L * 1000L;
    /**
     * socketPart清理时间间隔
     */
    public static Long SOCKET_PART_CLEAR_INTERVAL_SECONDS = 10L;

}
