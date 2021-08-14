package renderEngine.particles;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.renderer.ShaderProgram;

public class ParticleShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/renderEngine/particles/particleVShader.glsl";
    private static final String FRAGMENT_FILE = "src/renderEngine/particles/particleFShader.glsl";

    private int location_projectionMatrix;
    private int location_numOfRows;

    public ParticleShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocation() {
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_numOfRows = super.getUniformLocation("numOfRows");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        //started in 1 - reading from 1 to next declaration (need , so banks 1-4)
        super.bindAttribute(1, "modelViewMatrix");
        super.bindAttribute(5, "texOffsets");
        super.bindAttribute(6, "blendFactor");
    }

    protected void loadProjectionMatrix(Matrix4f projectionMatrix) {
        super.loadMatrix(location_projectionMatrix, projectionMatrix);
    }

    protected void loadNumberOfRows(float numRows) {
        super.loadFloat(location_numOfRows, numRows);
    }
}