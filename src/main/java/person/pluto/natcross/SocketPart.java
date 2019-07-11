package person.pluto.natcross;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 11:22:14
 */
@Data
public class SocketPart {

    private String socketPartKey;
    private Socket listenSocket;
    private Socket sendSocket;

    private InputToOutputThread inputToOutputThread;

    public void cancell() {

    }

    public void createPassWay() {
        InputStreamReader lisReader;
        try {
            lisReader = new InputStreamReader(listenSocket.getInputStream());
            OutputStreamWriter lisWriter = new OutputStreamWriter(listenSocket.getOutputStream());

            InputStreamReader sendReader = new InputStreamReader(sendSocket.getInputStream());
            OutputStreamWriter sendWriter = new OutputStreamWriter(sendSocket.getOutputStream());

            InputToOutputThread lisToSend = new InputToOutputThread(lisReader, sendWriter);
            InputToOutputThread sendToLis = new InputToOutputThread(sendReader, lisWriter);

            lisToSend.start();
            sendToLis.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
