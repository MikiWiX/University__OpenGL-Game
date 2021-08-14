package renderEngine.gui;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.renderer.ShaderProgram;

public class GuiShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/renderEngine/gui/GuiVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src/renderEngine/gui/GuiFragmentShader.glsl";

    //sore returned uniform location
    private int location_transformationMatrix;

    public GuiShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        //we want to bind attribute 0 of VAO - there we store position of vertex
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocation() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
    }

    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

}
