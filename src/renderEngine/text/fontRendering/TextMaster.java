package renderEngine.text.fontRendering;

import renderEngine.text.fontMeshCreator.FontType;
import renderEngine.text.fontMeshCreator.GUIText;
import renderEngine.text.fontMeshCreator.TextMeshData;
import renderEngine.storage.Model;
import renderEngine.storage.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextMaster {

    private Loader loader;
    private Map<FontType, List<GUIText>> texts = new HashMap<FontType, List<GUIText>>();
    private FontRenderer renderer;

    public TextMaster(Loader theLoader) {
        renderer = new FontRenderer();
        loader = theLoader;
    }

    public void render() {
        renderer.render(texts);
    }

    public void loadText(GUIText text) {
        FontType font = text.getFont();
        TextMeshData data = font.loadText(text);
        Model model = loader.createTextModel(data.getVertexPositions(), data.getTextureCoords());
        try {
            text.setMeshInfo(model.getVao(), data.getVertexCount());
            List<GUIText> textBatch = texts.get(font);
            if (textBatch == null) {
                textBatch = new ArrayList<>();
                texts.put(font, textBatch);
            }
            textBatch.add(text);
        } catch (NullPointerException e) {
            System.err.println("Error loading text");
        }
    }

    public void removeText(GUIText text) {
        List<GUIText> textBatch = texts.get(text.getFont());
        textBatch.remove(text);
        if(textBatch.isEmpty()) {
            texts.remove(text.getFont());
        }
    }

    public void cleanUP() {
        renderer.cleanUP();
    }
}
