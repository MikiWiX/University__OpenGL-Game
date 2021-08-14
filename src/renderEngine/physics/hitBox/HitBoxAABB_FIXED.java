package renderEngine.physics.hitBox;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;
import java.util.List;

public class HitBoxAABB_FIXED extends HitBoxHULL {

    protected List<Vector3f> size;

    private HitBoxOBB hbObb;

    public HitBoxAABB_FIXED(List<Vector3f> points){
        super(pointsFromSize(calculateSize(points)), Arrays.asList(new Vector3f(1,0,0), new Vector3f(0,1,0), new Vector3f(0,0,1)) );
        this.size = calculateSize(points);
    }

    public HitBoxAABB_FIXED(List<Vector3f> points, float radius){
        super(pointsFromSize(calculateSize(points)), Arrays.asList(new Vector3f(1,0,0), new Vector3f(0,1,0), new Vector3f(0,0,1)), radius);
        this.size = calculateSize(points);
    }

    public HitBoxOBB getAsOBB() {
        if(hbObb == null) {
            Vector3f center = calculateCenter(size);
            List<Float> extents = calculateExtents(size, normals, center);
            this.hbObb = new HitBoxOBB(points, normals, center, extents);
        }
        return hbObb;
    }

    @Override
    public HitBoxAABB_FIXED createWorldHitBox(Matrix4f transformationMatrix, Matrix4f rotationMatrix, Vector3f entityScale, Vector3f entityPosition) {
        List<Vector3f> worldSize = toWorldSpace(size, entityPosition, entityScale);
        float worldRadius = rescaleRadius(this.radius, entityScale);
        return new HitBoxAABB_FIXED(worldSize, worldRadius);
    }

    @Override
    public List<float[]> getOwnWorldSATTest() {
        if(selfSATTest == null) {
            selfSATTest = Arrays.asList(
                    new float[]{ size.get(0).x, size.get(1).x },
                    new float[]{ size.get(0).y, size.get(1).y },
                    new float[]{ size.get(0).z, size.get(1).z }
            );
        }
        return selfSATTest;
    }

    public List<Vector3f> getSize() {
        return size;
    }

    @Override
    public Vector3f getCenter() {
        return calculateCenter(size);
    }
}
