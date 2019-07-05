package person.pluto.natcross;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * <p>
 * 输入流对输出流 直接输出
 * </p>
 *
 * @author wangmin1994@qq.com
 * @since 2019-07-05 10:20:33
 */
public class InputToOutputThread extends Thread {

    private boolean isAlive = true;
    private Reader reader;
    private Writer writer;

    public InputToOutputThread(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void run() {
        int len = -1;
        char[] arrayTemp = new char[1024];
        try {
            while (isAlive && (len = reader.read(arrayTemp)) > 0) {
                writer.write(arrayTemp, 0, len);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancell() {
        isAlive = false;
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
