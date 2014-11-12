package coffee.letsgo.avatar.server;

import coffee.letsgo.avatar.Avatar;
import coffee.letsgo.avatar.AvatarInfo;
import coffee.letsgo.avatar.AvatarNotFoundException;
import coffee.letsgo.avatar.NotSupportedFormatException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xbwu on 11/10/14.
 */
public class AvatarImpl implements Avatar {
    private static Logger logger = LoggerFactory.getLogger(AvatarImpl.class);

    @Override
    public AvatarInfo put(long userId, byte[] img)
            throws NotSupportedFormatException, TException {

        BufferedImage bufferedImage = readImage(img);
        return new AvatarInfo();
    }

    @Override
    public byte[] get(AvatarInfo info)
            throws AvatarNotFoundException, TException {

        return new byte[0];
    }

    private BufferedImage readImage(byte[] data) throws NotSupportedFormatException {
        try {
            InputStream in = new ByteArrayInputStream(data);
            return ImageIO.read(in);
        } catch (IOException ex) {
            logger.error("failed to read image data", ex);
            NotSupportedFormatException nsfe = new NotSupportedFormatException();
            nsfe.setMsg("not able to read image data");
            throw nsfe;
        }
    }
}
