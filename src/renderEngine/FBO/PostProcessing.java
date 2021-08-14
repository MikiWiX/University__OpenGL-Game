package renderEngine.FBO;

import renderEngine.FBO.ContrastChanger.ContrastChanger;
import renderEngine.storage.Model;
import renderEngine.storage.Loader;
import org.lwjgl.opengl.GL11;

public class PostProcessing {

    private final float[] POSITIONS = { -1f, 1f, -1f, -1f, 1, 1, 1, -1 };
    private Model quad;
    private ContrastChanger contrastChanger;

    public PostProcessing(Loader loader){
        quad = loader.createGuiModel(POSITIONS);
        contrastChanger = new ContrastChanger();
    }

    public void doPostProcessing(int colourTexture){
        start();
        //post processing pipeline here
        contrastChanger.render(colourTexture);
        end();
    }

    public void cleanUP(){
        contrastChanger.cleanUP();
    }

    private void start(){
        quad.getVao().bind(0);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    private void end(){
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        quad.getVao().unbind(0);
    }
}
