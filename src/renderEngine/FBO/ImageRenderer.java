package renderEngine.FBO;

import static org.lwjgl.opengl.GL11.*;

public class ImageRenderer {

    private ScreenFBO fbo;

    //... to another fbo
    public ImageRenderer(int inWidth, int inHeight) {
        this.fbo = new ScreenFBO(inWidth, inHeight, ScreenFBO.NONE);
    }

    public ImageRenderer() {}

    //render to screen
    public void renderQuad() {
        if (fbo != null) {
            fbo.bindFrameBuffer();
        }
        glClear(GL_COLOR_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        if (fbo != null) {
            fbo.unbindFrameBuffer();
        }
    }

    public int getOutputTexture() {
        return fbo.getColourTexture();
    }

    public void cleanUP() {
        if (fbo != null) {
            fbo.cleanUP();
        }
    }

}
