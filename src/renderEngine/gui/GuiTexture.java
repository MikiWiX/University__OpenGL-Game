package renderEngine.gui;

import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.GameMain;

public class GuiTexture {

    private Vector2f originPosition;
    private Vector2f originScale;

    private int texture;
    private Vector2f position;
    private Vector2f scale;
    private float rotZ;

    public GuiTexture(int texture, Vector2f position, Vector2f scale, float rotZ) {
        this.texture = texture;
        this.position = position;
        this.scale = scale;
        this.rotZ = rotZ;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setScale(Vector2f scale) {
        this.scale = scale;
    }

    public void resetGuiPlacementNRotation(Vector2f position, Vector2f scale, float rotZ) {
        this.position = position;
        this.scale = scale;
        this.rotZ = rotZ;
    }

    public void resetGuiPlacement(Vector2f position, Vector2f scale) {
        this.position = position;
        this.scale = scale;
    }

    public void rotateGui(float rotation) {
        rotZ = rotZ + (rotation * GameMain.getFrameRenderTime());
    }

    public int getTexture() {
        return texture;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getScale() {
        return scale;
    }

    public float getRotZ() {
        return rotZ;
    }

    public Vector2f getOriginPosition() {
        return originPosition;
    }

    public Vector2f getOriginScale() {
        return originScale;
    }
}
