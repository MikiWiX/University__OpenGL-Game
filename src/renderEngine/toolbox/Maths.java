package renderEngine.toolbox;

import renderEngine.entities.Entity;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix3f;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

public class Maths {

    public static Matrix4f createGuiTransformationMatrix(Vector2f translation, Vector2f scale, float rotation) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.scale(new Vector3f(scale.x,scale.y,1f), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0,0,1), matrix, matrix);
        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, Vector3f scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);
        Matrix4f.scale(scale, matrix, matrix);
        return matrix;
    }

    public static Matrix4f createTransformationMatrixFromEntity(Entity entity) {
        return (createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale()));
    }

    public static Matrix4f createRotationMatrix4f(float rx, float ry, float rz) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1,0,0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0,1,0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0,0,1), matrix, matrix);

       return matrix;
    }
}
