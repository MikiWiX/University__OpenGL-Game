package renderEngine.entities.cameras;

import Input.Input;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.GameMain;
import Input.Settings;

import java.util.ArrayList;

public abstract class Camera {

    protected Vector3f position;
    protected float pitchDX;
    protected float yawDY;
    protected float rollDZ;

    public static float FOV = Settings.FOV;
    public static float NEAR_PLANE = Settings.RENDER_MIN_DISTANCE;
    public static float FAR_PLANE = Settings.RENDER_MAX_DISTANCE;
    private Matrix4f projectionMatrix;

    public Camera(Vector3f position, float dx, float dy, float dz) {
        this.position = position;
        this.pitchDX = dx;
        this.yawDY = dy;
        this.rollDZ = dz;
    }

    public abstract void move();

    public Vector3f getPosition() {
        return position;
    }

    public float getPitchDX() {
        return pitchDX;
    }

    public float getYawDY() {
        return yawDY;
    }

    public float getRollDZ() {
        return rollDZ;
    }

    public Matrix4f getProjectionMatrix() {
        return  projectionMatrix;
    }

    public Matrix4f createProjectionMatrix() {
        float aspectRatio = (float) GameMain.getInWidth()/ GameMain.getInHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV/2f))));
        float x_scale = (float) y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
        return projectionMatrix;
    }

    public Matrix4f createViewMatrix() {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();
        Matrix4f.rotate((float) Math.toRadians(pitchDX), new Vector3f(1,0,0), viewMatrix, viewMatrix);
        Matrix4f.rotate((float) Math.toRadians(yawDY), new Vector3f(0,1,0), viewMatrix, viewMatrix);
        Matrix4f.rotate((float) Math.toRadians(rollDZ), new Vector3f(0,0,1), viewMatrix, viewMatrix);
        Vector3f negativeCameraPos = new Vector3f(-position.x, -position.y, -position.z);
        Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
        return viewMatrix;
    }

}
