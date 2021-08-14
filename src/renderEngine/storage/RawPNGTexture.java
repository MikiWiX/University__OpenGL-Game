package renderEngine.storage;

import java.nio.ByteBuffer;

public class RawPNGTexture {

    private int width;
    private int height;
    private ByteBuffer image;

    public RawPNGTexture(int width, int height, ByteBuffer image) {
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getImage() {
        return image;
    }

}
