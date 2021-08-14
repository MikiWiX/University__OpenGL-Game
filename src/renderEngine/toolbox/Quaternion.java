package renderEngine.toolbox;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;

public class Quaternion {
    private float x;
    private float y;
    private float z;
    private float w;

    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.normalize();
    }

    public Quaternion(Matrix4f matrix) {
        float diagonal = matrix.m00 + matrix.m11 + matrix.m22;
        float y4;
        if (diagonal > 0.0F) {
            y4 = (float)(Math.sqrt((double)(diagonal + 1.0F)) * 2.0D);
            this.w = y4 / 4.0F;
            this.x = (matrix.m21 - matrix.m12) / y4;
            this.y = (matrix.m02 - matrix.m20) / y4;
            this.z = (matrix.m10 - matrix.m01) / y4;
        } else if (matrix.m00 > matrix.m11 && matrix.m00 > matrix.m22) {
            y4 = (float)(Math.sqrt((double)(1.0F + matrix.m00 - matrix.m11 - matrix.m22)) * 2.0D);
            this.w = (matrix.m21 - matrix.m12) / y4;
            this.x = y4 / 4.0F;
            this.y = (matrix.m01 + matrix.m10) / y4;
            this.z = (matrix.m02 + matrix.m20) / y4;
        } else if (matrix.m11 > matrix.m22) {
            y4 = (float)(Math.sqrt((double)(1.0F + matrix.m11 - matrix.m00 - matrix.m22)) * 2.0D);
            this.w = (matrix.m02 - matrix.m20) / y4;
            this.x = (matrix.m01 + matrix.m10) / y4;
            this.y = y4 / 4.0F;
            this.z = (matrix.m12 + matrix.m21) / y4;
        } else {
            y4 = (float)(Math.sqrt((double)(1.0F + matrix.m22 - matrix.m00 - matrix.m11)) * 2.0D);
            this.w = (matrix.m10 - matrix.m01) / y4;
            this.x = (matrix.m02 + matrix.m20) / y4;
            this.y = (matrix.m12 + matrix.m21) / y4;
            this.z = y4 / 4.0F;
        }

        this.normalize();
    }

    public Matrix4f toRotationMatrix() {
        Matrix4f matrix = new Matrix4f();
        float xy = this.x * this.y;
        float xz = this.x * this.z;
        float xw = this.x * this.w;
        float yz = this.y * this.z;
        float yw = this.y * this.w;
        float zw = this.z * this.w;
        float xSquared = this.x * this.x;
        float ySquared = this.y * this.y;
        float zSquared = this.z * this.z;
        matrix.m00 = 1.0F - 2.0F * (ySquared + zSquared);
        matrix.m01 = 2.0F * (xy - zw);
        matrix.m02 = 2.0F * (xz + yw);
        matrix.m03 = 0.0F;
        matrix.m10 = 2.0F * (xy + zw);
        matrix.m11 = 1.0F - 2.0F * (xSquared + zSquared);
        matrix.m12 = 2.0F * (yz - xw);
        matrix.m13 = 0.0F;
        matrix.m20 = 2.0F * (xz - yw);
        matrix.m21 = 2.0F * (yz + xw);
        matrix.m22 = 1.0F - 2.0F * (xSquared + ySquared);
        matrix.m23 = 0.0F;
        matrix.m30 = 0.0F;
        matrix.m31 = 0.0F;
        matrix.m32 = 0.0F;
        matrix.m33 = 1.0F;
        return matrix;
    }

    public void normalize() {
        float mag = (float)Math.sqrt((double)(this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z));
        this.w /= mag;
        this.x /= mag;
        this.y /= mag;
        this.z /= mag;
    }

    public static Quaternion slerp(Quaternion start, Quaternion end, float progression) {
        start.normalize();
        end.normalize();
        float d = start.x * end.x + start.y * end.y + start.z * end.z + start.w * end.w;
        float absDot = d < 0.0F ? -d : d;
        float scale0 = 1.0F - progression;
        float scale1 = progression;
        float newX;
        float newY;
        if (1.0F - absDot > 0.1F) {
            newX = (float)Math.acos((double)absDot);
            newY = 1.0F / (float)Math.sin((double)newX);
            scale0 = (float)Math.sin((double)((1.0F - progression) * newX)) * newY;
            scale1 = (float)Math.sin((double)(progression * newX)) * newY;
        }

        if (d < 0.0F) {
            scale1 = -scale1;
        }

        newX = scale0 * start.x + scale1 * end.x;
        newY = scale0 * start.y + scale1 * end.y;
        float newZ = scale0 * start.z + scale1 * end.z;
        float newW = scale0 * start.w + scale1 * end.w;
        return new Quaternion(newX, newY, newZ, newW);
    }

    public String toString() {
        return this.x + ", " + this.y + ", " + this.z + ", " + this.w;
    }
}
