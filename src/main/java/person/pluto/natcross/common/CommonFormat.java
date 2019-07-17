package person.pluto.natcross.common;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * <p>
 * 
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 13:35:04
 */
public class CommonFormat {

    /**
     * 获取socket匹配对key
     *
     * @author Pluto
     * @since 2019-07-17 09:35:10
     * @param listenPort
     * @return
     */
    public static String getSocketPartKey(Integer listenPort) {
        DecimalFormat fiveLenFormat = new DecimalFormat("00000");
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String randomNum = RandomStringUtils.randomNumeric(4);
        return "SK-" + fiveLenFormat.format(listenPort) + "-" + dateTime + "-" + randomNum;
    }

    /**
     * 根据socketPartKey获取端口号
     *
     * @author Pluto
     * @since 2019-07-17 11:39:50
     * @param socketPartKey
     * @return
     */
    public static Integer getSocketPortByPartKey(String socketPartKey) {
        String[] split = socketPartKey.split("-");
        return Integer.valueOf(split[1]);
    }

    /**
     * 获取交互流水号
     *
     * @author Pluto
     * @since 2019-07-17 09:35:29
     * @return
     */
    public static String getInteractiveSeq() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String randomNum = RandomStringUtils.randomNumeric(4);
        return "IS-" + dateTime + "-" + randomNum;
    }

}
