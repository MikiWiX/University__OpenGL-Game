package renderEngine.text.fontMeshCreator;

import java.io.File;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

/**
 * Represents a font. It holds the font's texture atlas as well as having the
 * ability to create the quad vertices for any renderEngine.text.renderEngine.text using this font.
 *
 * @author Karl
 *
 */
public class FontType {

    private int textureAtlas;
    private TextMeshCreator loader;

    /**
     * Creates a new font and loads up the data about each character from the
     * font file.
     *
     * @param textureAtlas
     *            - the ID of the font atlas texture.
     * @param fontFile
     *            - the font file containing information about each character in
     *            the texture atlas.
     */
    public FontType(int textureAtlas, File fontFile, int padding) {
        this.textureAtlas = textureAtlas;
        this.loader = new TextMeshCreator(fontFile, padding);
    }

    /**
     * @return The font texture atlas.
     */
    public int getTextureAtlas() {
        return textureAtlas;
    }

    /**
     * Takes in an unloaded renderEngine.text.renderEngine.text and calculate all of the vertices for the quads
     * on which this renderEngine.text.renderEngine.text will be rendered. The vertex positions and texture
     * coords and calculated based on the information from the font file.
     *
     * @param text
     *            - the unloaded renderEngine.text.renderEngine.text.
     * @return Information about the vertices of all the quads.
     */
    public TextMeshData loadText(GUIText text) {
        return loader.createTextMesh(text);
    }

    public void cleanUP() {
        glDeleteTextures(textureAtlas);
    }

}