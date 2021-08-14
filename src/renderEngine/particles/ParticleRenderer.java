package renderEngine.particles;

import Input.Config;
import renderEngine.entities.cameras.Camera;
import renderEngine.storage.Model;
import renderEngine.storage.Loader;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

public class ParticleRenderer {

    // quad
    private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
    // flexible, we declare one particle buffer in order to not reallocate it every frame
    // set that to vboData size OR MAKE SURE IT WON'T OVERFLOW!!!
    private static int MAX_INSTANCES = Config.MAX_PARTICLES;
    // data (in number of floats) per instance
    // 4x4 matrix + (2+2)textureOffsets + 1 blendFactor
    private static final int INSTANCE_DATA_LENGTH = 21;

    private static FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_INSTANCES*INSTANCE_DATA_LENGTH);

    private Model quad;
    private ParticleShader shader;

    private Loader loader;
    private ParticleVBO particleVBO;
    private int vbo;
    private int floatArrayPointer = 0;

    protected ParticleRenderer(Loader loader, Matrix4f projectionMatrix){
        this.loader = loader;
        this.particleVBO = new ParticleVBO();

        //creates and glBufferData en (yet) empty VBO
        this.vbo = particleVBO.createEmptyVBO(INSTANCE_DATA_LENGTH * MAX_INSTANCES);
        // creates vao and load quad to vbo ID 0 as usually
        quad = loader.createGuiModel(VERTICES);
        //add VBOs X to quad VAO, linked to (yet) empty VBO by given parameters
        //those VBOs will be IN variables of vertex shader (not uniforms)
        //updating a single VBO is WAY more efficient than 10000 uniforms each frame

            //load Matrix row 1-4
            particleVBO.addInstanceAttribute(quad.getVao().id, vbo, 1, 4, INSTANCE_DATA_LENGTH, 0);
            particleVBO.addInstanceAttribute(quad.getVao().id, vbo, 2, 4, INSTANCE_DATA_LENGTH, 4);
            particleVBO.addInstanceAttribute(quad.getVao().id, vbo, 3, 4, INSTANCE_DATA_LENGTH, 8);
            particleVBO.addInstanceAttribute(quad.getVao().id, vbo, 4, 4, INSTANCE_DATA_LENGTH, 12);
            //load Offsets
            particleVBO.addInstanceAttribute(quad.getVao().id, vbo, 5, 4, INSTANCE_DATA_LENGTH, 16);
            //load Blend
            particleVBO.addInstanceAttribute(quad.getVao().id, vbo, 6, 1, INSTANCE_DATA_LENGTH, 20);

        shader = new ParticleShader();
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    protected void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    protected void render(Map<ParticleTexture, List<Particle>> particleList, Camera camera){
        Matrix4f viewMatrix = camera.createViewMatrix();
        prepare();

        for(ParticleTexture texture: particleList.keySet()) {
            bindTexture(texture);
            List<Particle> particles = particleList.get(texture);
            floatArrayPointer = 0;
            float[] vboData = new float[particles.size()*INSTANCE_DATA_LENGTH];

            for (Particle particle : particles) {
                updateModelViewMatrix(vboData, particle.getPosition(), particle.getRotation(), particle.getScale(), viewMatrix);
                updateTexCoordinatesInfo(particle, vboData);
            }

            if(Config.DYNAMIC_MAX_PARTICLES){
                MAX_INSTANCES = particles.size();
                buffer = BufferUtils.createFloatBuffer(MAX_INSTANCES*INSTANCE_DATA_LENGTH);
            }

            particleVBO.updateVbo(vbo, vboData, buffer);
            GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, quad.getVao().getVertexCount(), particles.size());
        }
        finishRendering();
    }

    private void bindTexture(ParticleTexture texture) {
        if(texture.getAlphaBlending()) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
        shader.loadNumberOfRows(texture.getNumberOfRows());
    }

    private void updateTexCoordinatesInfo(Particle particle, float[] vboData) {
        vboData[floatArrayPointer++] = particle.getTexOffset1().x;
        vboData[floatArrayPointer++] = particle.getTexOffset1().y;
        vboData[floatArrayPointer++] = particle.getTexOffset2().x;
        vboData[floatArrayPointer++] = particle.getTexOffset2().y;
        vboData[floatArrayPointer++] = particle.getBlend();
    }

    private void updateModelViewMatrix(float[] vboData, Vector3f position, float rotation, float scale, Matrix4f viewMatrix) {
        Matrix4f modelMatrix = new Matrix4f();
        Matrix4f.translate(position, modelMatrix, modelMatrix);
        // NO ROTATION -> model matrix * view matrix = top left 3x3 part is an uniform matrix
        //need to transpose that 3x3 part
        modelMatrix.m00 = viewMatrix.m00;
        modelMatrix.m01 = viewMatrix.m10;
        modelMatrix.m02 = viewMatrix.m20;
        modelMatrix.m10 = viewMatrix.m01;
        modelMatrix.m11 = viewMatrix.m11;
        modelMatrix.m12 = viewMatrix.m21;
        modelMatrix.m20 = viewMatrix.m02;
        modelMatrix.m21 = viewMatrix.m12;
        modelMatrix.m22 = viewMatrix.m22;
        Matrix4f.rotate((float)Math.toRadians(rotation), new Vector3f(0, 0, 1), modelMatrix, modelMatrix);
        Matrix4f.scale(new Vector3f(scale, scale, scale), modelMatrix, modelMatrix);
        Matrix4f modelViewMatrix = Matrix4f.mul(viewMatrix, modelMatrix, null);
        storeMatrixDataInVbo(modelViewMatrix, vboData);
    }

    private void storeMatrixDataInVbo(Matrix4f modelViewMatrix, float[] vboData) {
        vboData[floatArrayPointer++] = modelViewMatrix.m00;
        vboData[floatArrayPointer++] = modelViewMatrix.m01;
        vboData[floatArrayPointer++] = modelViewMatrix.m02;
        vboData[floatArrayPointer++] = modelViewMatrix.m03;
        vboData[floatArrayPointer++] = modelViewMatrix.m10;
        vboData[floatArrayPointer++] = modelViewMatrix.m11;
        vboData[floatArrayPointer++] = modelViewMatrix.m12;
        vboData[floatArrayPointer++] = modelViewMatrix.m13;
        vboData[floatArrayPointer++] = modelViewMatrix.m20;
        vboData[floatArrayPointer++] = modelViewMatrix.m21;
        vboData[floatArrayPointer++] = modelViewMatrix.m22;
        vboData[floatArrayPointer++] = modelViewMatrix.m23;
        vboData[floatArrayPointer++] = modelViewMatrix.m30;
        vboData[floatArrayPointer++] = modelViewMatrix.m31;
        vboData[floatArrayPointer++] = modelViewMatrix.m32;
        vboData[floatArrayPointer++] = modelViewMatrix.m33;
    }

    protected void cleanUP(){
        shader.cleanUP();
        loader.cleanUP();
    }

    protected void prepare(){
        shader.start();
        GL30.glBindVertexArray(quad.getVao().id);
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glEnableVertexAttribArray(2);
        GL30.glEnableVertexAttribArray(3);
        GL30.glEnableVertexAttribArray(4);
        GL30.glEnableVertexAttribArray(5);
        GL30.glEnableVertexAttribArray(6);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
    }

    protected void finishRendering(){
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glDisableVertexAttribArray(2);
        GL30.glDisableVertexAttribArray(3);
        GL30.glDisableVertexAttribArray(4);
        GL30.glDisableVertexAttribArray(5);
        GL30.glDisableVertexAttribArray(6);
        GL30.glBindVertexArray(0);
        shader.stop();
    }
}