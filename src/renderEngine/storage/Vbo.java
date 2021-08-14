package renderEngine.storage;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class Vbo {
    private final int vboId;
    private final int type;

    private Vbo(int vboId, int type) {
        this.vboId = vboId;
        this.type = type;
    }

    public static Vbo create(int type) {
        int id = GL15.glGenBuffers();
        return new Vbo(id, type);
    }

    public void bind() {
        GL15.glBindBuffer(this.type, this.vboId);
    }

    public void unbind() {
        GL15.glBindBuffer(this.type, 0);
    }

    public void storeData(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        this.storeData(buffer);
    }

    public void storeData(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        this.storeData(buffer);
    }

    public void storeData(IntBuffer data) {
        GL15.glBufferData(this.type, data, GL_STATIC_DRAW);
    }

    public void storeData(FloatBuffer data) {
        GL15.glBufferData(this.type, data, GL_STATIC_DRAW);
    }

    public void delete() {
        GL15.glDeleteBuffers(this.vboId);
    }
}
