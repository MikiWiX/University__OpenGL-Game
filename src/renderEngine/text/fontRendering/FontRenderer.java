package renderEngine.text.fontRendering;

import renderEngine.text.fontMeshCreator.FontType;
import renderEngine.text.fontMeshCreator.GUIText;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.List;
import java.util.Map;

public class FontRenderer {

    private FontShader shader;

    public FontRenderer() {
        shader = new FontShader();
    }

    public void render(Map<FontType, List<GUIText>> texts) {
        prepare();
        for(FontType font : texts.keySet()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL13.glBindTexture(GL11.GL_TEXTURE_2D, font.getTextureAtlas());
            for (GUIText text : texts.get(font)) {
                renderText(text);
            }
        }
        endRendering();
    }

    public void cleanUP(){
        shader.cleanUP();
    }

    private void prepare(){
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        shader.start();
    }

    private void renderText(GUIText text){
        text.getVao().bind(0,1);
        shader.loadCharacterProperties(text.getCharacterColor(), text.getCharacterWidth(), text.getCharacterEdge());
        shader.loadBorderProperties(text.getBorderColor(), text.getBorderWidth(), text.getBorderEdge(), text.getBorderOffset());
        shader.loadTranslation(text.getPosition());
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());
        text.getVao().unbind(0,1);
    }

    private void endRendering(){
        shader.stop();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

}