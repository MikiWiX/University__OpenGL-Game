package renderEngine.FBO.ContrastChanger;

import renderEngine.FBO.ImageRenderer;
import Input.Settings;

import static org.lwjgl.opengl.GL13.*;

public class ContrastChanger {

    private ImageRenderer renderer;
    private ContrastShader shader;

    public ContrastChanger() {
        shader = new ContrastShader();
        renderer = new ImageRenderer();
    }

    public void render(int texture) {
        shader.start();
        shader.loadBrightness(Settings.BRIGHTNESS);
        shader.loadContrast(Settings.CONTRAST);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        renderer.renderQuad();
        shader.stop();
    }

    public void cleanUP() {
        renderer.cleanUP();
        shader.cleanUP();
    }

}
