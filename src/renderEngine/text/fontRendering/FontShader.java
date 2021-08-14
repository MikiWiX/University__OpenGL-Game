package renderEngine.text.fontRendering;

import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.renderer.ShaderProgram;

public class FontShader extends ShaderProgram{

    private static final String VERTEX_FILE = "src/renderEngine/text/fontRendering/fontVertex.glsl";
    private static final String FRAGMENT_FILE = "src/renderEngine/text/fontRendering/fontFragment.glsl";

    private int location_translation;

    private int location_characterColor;
    private int location_characterWidth;
    private int location_characterEdge;

    private int location_borderColor;
    private int location_borderWidth;
    private int location_borderEdge;
    private int location_borderOffset;

    public FontShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocation() {
        location_translation = super.getUniformLocation("translation");

        location_characterColor = super.getUniformLocation("characterColor");
        location_characterWidth = super.getUniformLocation("characterWidth");
        location_characterEdge = super.getUniformLocation("characterEdge");

        location_borderColor = super.getUniformLocation("borderColor");
        location_borderWidth = super.getUniformLocation("borderWidth");
        location_borderEdge = super.getUniformLocation("borderEdge");
        location_borderOffset = super.getUniformLocation("borderOffset");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCords");
    }

    protected void loadTranslation(Vector2f translation) {
        super.loadVector2f(location_translation, translation);
    }

    protected void loadCharacterProperties(Vector3f color, float width, float edge) {
        super.loadVector3f(location_characterColor, color);
        super.loadFloat(location_characterWidth, width);
        super.loadFloat(location_characterEdge, edge);
    }

    protected void loadBorderProperties(Vector3f color, float width, float edge, Vector2f offset) {
        super.loadVector3f(location_borderColor, color);
        super.loadFloat(location_borderWidth, width);
        super.loadFloat(location_borderEdge, edge);
        super.loadVector2f(location_borderOffset, offset);
    }
}