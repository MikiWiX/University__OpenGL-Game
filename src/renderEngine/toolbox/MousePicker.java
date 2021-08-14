package renderEngine.toolbox;

import renderEngine.entities.cameras.Camera;
import org.lwjgl.BufferUtils;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector4f;
import renderEngine.GameMain;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;

public class MousePicker {

    private Vector3f currentRay;

    private Matrix4f invertedProjectionMatrix;
    private Matrix4f invertedViewMatrix;
    private Camera camera;

    public MousePicker(Camera cam, Matrix4f projectionMatrix) {
        this.camera = cam;
        this.invertedProjectionMatrix = Matrix4f.invert(projectionMatrix, null);
        this.invertedViewMatrix = Matrix4f.invert(camera.createViewMatrix(), null);
    }

    public void reset(Matrix4f projectionMatrix) {
        this.invertedProjectionMatrix = Matrix4f.invert(projectionMatrix, null);
    }

    public Vector3f getCurrentRay() {
        return currentRay;
    }

    public void update() {
        invertedViewMatrix = Matrix4f.invert(camera.createViewMatrix(), null);
        currentRay = calculateMouseRay();
        //System.out.println(String.valueOf(currentRay.x)+"   "+String.valueOf(currentRay.y)+"   "+String.valueOf(currentRay.z));
    }

    private Vector3f calculateMouseRay() {

        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

        glfwGetCursorPos(GameMain.getWindow(), x, y);
        x.rewind();
        y.rewind();

        float mouseX = (float) x.get();
        float mouseY = (float) y.get();

        Vector2f normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY);
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1f, 1f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        return toWorldCoords(eyeCoords);
    }

    private Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY) {
        float x = (2*mouseX) / GameMain.getWidth() -1;
        float y = (2*mouseY) / GameMain.getHeight() -1;
        return new Vector2f(x, y);
    }

    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Vector4f eyeCoords =  Matrix4f.transform(invertedProjectionMatrix, clipCoords, null);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Vector4f worldray = Matrix4f.transform(invertedViewMatrix, eyeCoords, null);
        Vector3f worldCoodrs = new Vector3f(worldray.x, worldray.y, worldray.z);
        worldCoodrs.normalise();
        return worldCoodrs;
    }
}
