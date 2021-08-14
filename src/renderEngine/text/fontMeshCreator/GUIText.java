package renderEngine.text.fontMeshCreator;

import renderEngine.text.fontRendering.TextMaster;
import renderEngine.storage.Vao;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

/**
 * Represents a piece of renderEngine.text.renderEngine.text in the game.
 *
 * @author Karl
 *
 */
public class GUIText {

    private String textString;
    private float fontSize;
    private TextMaster textMaster;

    private Vao textMeshVao;
    private int vertexCount;

    private Vector2f position;
    private float lineMaxSize;
    private int numberOfLines;

    private FontType font;

    private boolean centerText = false;
    private Vector3f characterColor = new Vector3f(0f, 0f, 0f);
    private float characterWidth = 0.5f;
    private float characterEdge = 0.1f;
    private Vector3f borderColor = new Vector3f(0f, 0f, 0f);
    private float borderWidth = 0f;
    private float borderEdge = 0.1f;
    private Vector2f borderOffset = new Vector2f(0f, 0f);

    /**
     * Creates a new renderEngine.text.renderEngine.text, loads the renderEngine.text.renderEngine.text's quads into a VAO, and adds the renderEngine.text.renderEngine.text
     * to the screen.
     *
     * @param text
     *            - the renderEngine.text.renderEngine.text.
     * @param fontSize
     *            - the font size of the renderEngine.text.renderEngine.text, where a font size of 1 is the
     *            default size.
     * @param font
     *            - the font that this renderEngine.text.renderEngine.text should use.
     * @param position
     *            - the position on the screen where the top left corner of the
     *            renderEngine.text.renderEngine.text should be rendered. The top left corner of the screen is
     *            (0, 0) and the bottom right is (1, 1).
     * @param maxLineLength
     *            - basically the width of the virtual page in terms of screen
     *            width (1 is full screen width, 0.5 is half the width of the
     *            screen, etc.) Text cannot go off the edge of the page, so if
     *            the renderEngine.text.renderEngine.text is longer than this length it will go onto the next
     *            line. When renderEngine.text.renderEngine.text is centered it is centered into the middle of
     *            the line, based on this line length value.
     * @param centered
     *            - whether the renderEngine.text.renderEngine.text should be centered or not.
     */
    public GUIText(String text, TextMaster textMaster, float fontSize, FontType font, Vector2f position, float maxLineLength,
                   boolean centered) {
        this.textString = text;
        this.fontSize = fontSize;
        this.font = font;
        this.position = position;
        this.lineMaxSize = maxLineLength;
        this.centerText = centered;
        this.textMaster = textMaster;
        // load renderEngine.text.renderEngine.text
        textMaster.loadText(this);
    }

    /**
     * Remove the renderEngine.text.renderEngine.text from the screen.
     */
    public void remove() {
        // remove renderEngine.text.renderEngine.text
        textMaster.removeText(this);
    }

    public void cleanUP() {
        textMeshVao.delete();

    }

    /**
     * @return The font used by this renderEngine.text.renderEngine.text.
     */
    public FontType getFont() {
        return font;
    }

    /**
     * @return The number of lines of renderEngine.text.renderEngine.text. This is determined when the renderEngine.text.renderEngine.text is
     *         loaded, based on the length of the renderEngine.text.renderEngine.text and the max line length
     *         that is set.
     */
    public int getNumberOfLines() {
        return numberOfLines;
    }

    /**
     * @return The position of the top-left corner of the renderEngine.text.renderEngine.text in screen-space.
     *         (0, 0) is the top left corner of the screen, (1, 1) is the bottom
     *         right.
     */
    public Vector2f getPosition() {
        return position;
    }

    /**
     * @return the ID of the renderEngine.text.renderEngine.text's VAO, which contains all the vertex data for
     *         the quads on which the renderEngine.text.renderEngine.text will be rendered.
     */
    public Vao getVao() {
        return textMeshVao;
    }

    /**
     * Set the VAO and vertex count for this renderEngine.text.renderEngine.text.
     *
     * @param vao
     *            - the VAO containing all the vertex data for the quads on
     *            which the renderEngine.text.renderEngine.text will be rendered.
     * @param verticesCount
     *            - the total number of vertices in all of the quads.
     */
    public void setMeshInfo(Vao vao, int verticesCount) {
        this.textMeshVao = vao;
        this.vertexCount = verticesCount;
    }

    /**
     * @return The total number of vertices of all the renderEngine.text.renderEngine.text's quads.
     */
    public int getVertexCount() {
        return this.vertexCount;
    }

    /**
     * @return the font size of the renderEngine.text.renderEngine.text (a font size of 1 is normal).
     */
    protected float getFontSize() {
        return fontSize;
    }

    /**
     * Sets the number of lines that this renderEngine.text.renderEngine.text covers (method used only in
     * loading).
     *
     * @param number
     */
    protected void setNumberOfLines(int number) {
        this.numberOfLines = number;
    }

    /**
     * @return {@code true} if the renderEngine.text.renderEngine.text should be centered.
     */
    protected boolean isCentered() {
        return centerText;
    }

    /**
     * @return The maximum length of a line of this renderEngine.text.renderEngine.text.
     */
    protected float getMaxLineSize() {
        return lineMaxSize;
    }

    /**
     * @return The string of renderEngine.text.renderEngine.text.
     */
    protected String getTextString() {
        return textString;
    }

    public Vector3f getCharacterColor() {
        return characterColor;
    }

    public void setCharacterColor(float r, float g, float b) {
        this.characterColor = new Vector3f(r, g, b);
    }

    public float getCharacterWidth() {
        return characterWidth;
    }

    public void setCharacterWidth(float characterWidth) {
        this.characterWidth = characterWidth;
    }

    public float getCharacterEdge() {
        return characterEdge;
    }

    public void setCharacterEdge(float characterEdge) {
        this.characterEdge = characterEdge;
    }

    public Vector3f getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(float r, float g, float b) {
        this.borderColor = new Vector3f(r, g, b);
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public float getBorderEdge() {
        return borderEdge;
    }

    public void setBorderEdge(float borderEdge) {
        this.borderEdge = borderEdge;
    }

    public Vector2f getBorderOffset() {
        return borderOffset;
    }

    public void setBorderOffset(float x, float y) {
        this.borderOffset = new Vector2f(x, y);
    }
}