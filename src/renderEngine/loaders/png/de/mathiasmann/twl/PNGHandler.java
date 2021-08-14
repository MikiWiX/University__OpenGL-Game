package renderEngine.loaders.png.de.mathiasmann.twl;

import renderEngine.loaders.png.de.mathiasmann.twl.utils.PNGDecoder;
import renderEngine.storage.RawPNGTexture;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class PNGHandler {

    public static RawPNGTexture decodeTextureFile(String path) {
        int width = 0;
        int height = 0;
        ByteBuffer buffer = null;

        try {
            InputStream in = new FileInputStream(System.getProperty("user.dir")+"/"+path);
            PNGDecoder decoder = new PNGDecoder(in);
            width = decoder.getWidth();
            height = decoder.getHeight();
            buffer = ByteBuffer.allocateDirect(4 * width * height);
            decoder.decode(buffer, width * 4, PNGDecoder.Format.RGBA);
            buffer.flip();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Tried to load texture " + path + " , didn't work");
            System.exit(-1);
        }

        return new RawPNGTexture(width, height, buffer);
    }
}
