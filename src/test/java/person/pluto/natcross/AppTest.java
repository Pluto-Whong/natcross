package person.pluto.natcross;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import person.pluto.natcross.common.CommonFormat;
import person.pluto.natcross.common.InteractiveUtil;
import person.pluto.natcross.model.InteractiveModel;
import person.pluto.natcross.model.InteractiveTypeEnum;

@SuppressWarnings("unused")
public class AppTest {

    @Test
    public void testApp() throws IOException {

        InteractiveModel interactiveModel = InteractiveModel.of(InteractiveTypeEnum.CLIENT_WAIT,
                CommonFormat.getSocketPartKey(8080));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        InteractiveUtil.send(outputStream, interactiveModel);

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        InteractiveModel recv = InteractiveUtil.recv(inputStream);

        System.out.println(recv.toJSONString());
    }

}
