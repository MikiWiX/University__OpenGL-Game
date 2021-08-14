package renderEngine.gui;

import renderEngine.storage.Model;
import renderEngine.storage.Loader;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.Maths;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class GuiRenderer {

    private final Model quad;
    private GuiShader shader;

    public GuiRenderer(Loader loader) {
        float[] positions = {-1,1, -1,-1, 1,1, 1,-1};
        quad = loader.createGuiModel(positions);
        shader = new GuiShader();
    }

    public void cleanUP() {
        shader.cleanUP();
    }

    public void render(List<GuiTexture> guis) {
        shader.start();
        quad.getVao().bind(0);

        //transparency - no problems with overlapping half-transparent objects so can use this way, also no need to depth test it which resolves that problem
        //enable alpha blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        //render
        for(GuiTexture gui: guis){
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, gui.getTexture());
            //Matrix4f matrix = Maths.createGuiTransformationMatrix(renderEngine.gui.getPosition(), renderEngine.gui.getScale());
            Matrix4f matrix = Maths.createGuiTransformationMatrix(gui.getPosition(), gui.getScale(), gui.getRotZ());
            shader.loadTransformationMatrix(matrix);
            //actual draw
            glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVao().getVertexCount());
        }
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        //end render
        quad.getVao().unbind(0);
        shader.stop();
    }
}
