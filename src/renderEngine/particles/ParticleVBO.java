package renderEngine.particles;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class ParticleVBO {

    private List<Integer> vbos = new ArrayList<>();

    public int createEmptyVBO(int floatCount) {
        int vbo = glGenBuffers();
        this.vbos.add(vbo);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, floatCount * 4, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    /**
     * LINK VAO->attributeNumberVBO to given places in glBufferData (loaded above)
     * @param vao common renderEngine.particles VAO
     * @param vbo big input Vbo
     * @param attributeNumber number of instanced Particles VBOs in VAO
     *                        vbo 0 is normal vbo containing quad positions
     *                  --- instanced rendering below with instanced VBOs ---
     *                        vbo 1-4 hold modelViewMatrix to every Particle
     *                        vbo 5 same but textureOffset
     *                        vbo 6 blendFactor
     * @param dataSize size of singleData in @attributeNumber VBOs above
     * @param elementSize size of a single instance data in Big Input VBO
     *                    - an element here contains all @attributeNumber VBOs data
     * @param offset where does dataSize(single particle VBO data) is held in
     *               elementSize(part of input containing all single particle VBOs data)
     */
    public void addInstanceAttribute(int vao, int vbo, int attributeNumber,
                                     int dataSize, int elementSize, int offset) {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // ACTUAL LINK
        // put VBO in VAO (arguments:
        // VBO id in VAO,
        // length of each element being saved to VBO from wherever
        // type of data,
        // if data is normalized,
        // --- last 2 arguments: from the buffer you read:
        // size of your sub-data you want from actual buffered-data
        // offset from beginning of buffered-data to sub-data
        glVertexAttribPointer(attributeNumber, dataSize, GL_FLOAT, false, elementSize*4, offset*4);
        // indicate that its per-instance field, it needs to be changed per 1 instance
        glVertexAttribDivisor(attributeNumber, 1);
        // unbind VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void updateVbo(int vbo, float[] data, FloatBuffer buffer) {
        buffer.clear();
        buffer.put(data);
        buffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        //reallocate buffer
        glBufferData(GL_ARRAY_BUFFER, buffer.capacity()*4, GL_STATIC_DRAW);
        //fill it, its reallocated so this works faster than just updating
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void cleanUP() {
        for (int vbo : vbos) {
            glDeleteBuffers(vbo);
        }
    }
}
