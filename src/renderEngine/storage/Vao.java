package renderEngine.storage;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

public class Vao {

    private static final int BYTES_PER_FLOAT = 4;
    public final int id;
    private List<Vbo> dataVbos = new ArrayList();
    private Vbo indexVbo;
    private int vertexCount;

    public static Vao create() {
        int id = GL30.glGenVertexArrays();
        return new Vao(id);
    }

    private Vao(int id) {
        this.id = id;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public void setVertexCount(int count) {
        this.vertexCount = count;
    }

    public void bind(int... attributes) {
        this.bind();
        int attributeAmount = attributes.length;

        for (int x : attributes) {
            GL20.glEnableVertexAttribArray(x);
        }

    }

    public void unbind(int... attributes) {
        int var4 = attributes.length;

        for (int i : attributes) {
            GL20.glDisableVertexAttribArray(i);
        }

        this.unbind();
    }

    public void createIndexBuffer(int[] indices) {
        this.indexVbo = Vbo.create(GL_ELEMENT_ARRAY_BUFFER);
        this.indexVbo.bind();
        this.indexVbo.storeData(indices);
        this.vertexCount = indices.length;
    }

    public void createAttribute(int attribute, float[] data, int attrSize) {
        Vbo dataVbo = Vbo.create(GL_ARRAY_BUFFER);
        dataVbo.bind();
        dataVbo.storeData(data);
        GL20.glVertexAttribPointer(attribute, attrSize, GL_FLOAT, false, attrSize * 4, 0L);
        dataVbo.unbind();
        this.dataVbos.add(dataVbo);
    }

    public void createIntAttribute(int attribute, int[] data, int attrSize) {
        Vbo dataVbo = Vbo.create(GL_ARRAY_BUFFER);
        dataVbo.bind();
        dataVbo.storeData(data);
        GL30.glVertexAttribIPointer(attribute, attrSize, GL_INT, attrSize * 4, 0L);
        dataVbo.unbind();
        this.dataVbos.add(dataVbo);
    }

    public void delete() {
        GL30.glDeleteVertexArrays(this.id);
        Iterator iterator = this.dataVbos.iterator();

        while(iterator.hasNext()) {
            Vbo vbo = (Vbo)iterator.next();
            vbo.delete();
        }
        if (indexVbo != null) {
            this.indexVbo.delete();
        }
    }

    private void bind() {
        GL30.glBindVertexArray(this.id);
    }

    private void unbind() {
        GL30.glBindVertexArray(0);
    }
}
