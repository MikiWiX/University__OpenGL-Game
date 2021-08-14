package renderEngine.FBO.ContrastChanger;

import renderEngine.renderer.ShaderProgram;

public class ContrastShader extends ShaderProgram {

    private static final String VERTEX_FILE = "src/renderEngine/FBO/ContrastChanger/PostProcessVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src/renderEngine/FBO/ContrastChanger/PostProcessFragmentShader.glsl";

    private int location_brightness;
    private int location_contrast;

    public ContrastShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        //we want to bind attribute 0 of VAO - there we store position of vertex
        super.bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocation() {
        location_brightness = super.getUniformLocation("brightness");
        location_contrast = super.getUniformLocation("contrast");
    }

    protected void loadBrightness(float brightness) {
        super.loadFloat(location_brightness, brightness);
    }

    protected void loadContrast(float contrast) {
        super.loadFloat(location_contrast, contrast);
    }
}