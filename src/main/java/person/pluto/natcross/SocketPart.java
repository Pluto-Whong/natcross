package person.pluto.natcross;

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
    
    
    public void cancell() {
        
    }

}
