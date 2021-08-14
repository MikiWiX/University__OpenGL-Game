package renderEngine.physics.hitBox;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class HitBoxOBB extends HitBoxHULL{

    protected Vector3f center;
    protected List<Float> extents;

    public HitBoxOBB(List<Vector3f> points, List<Vector3f> normals){
        super(points, normals);
        this.center = calculateCenter(calculateSize(points));
        this.extents = calculateExtents(points, normals, center);
    }

    public HitBoxOBB(List<Vector3f> points, List<Vector3f> normals, float radius){
        super(points, normals, radius);
        this.center = calculateCenter(calculateSize(points));
        this.extents = calculateExtents(points, normals, center);
        this.radius = radius;
    }

    public HitBoxOBB(List<Vector3f> points, List<Vector3f> normals, Vector3f center, List<Float> extents){
        super(points, normals);
        this.center = center;
        this.extents = extents;
    }

    public HitBoxOBB(List<Vector3f> points, List<Vector3f> normals, Vector3f center, List<Float> extents, float radius){
        super(points, normals, radius);
        this.center = center;
        this.extents = extents;
        this.radius = radius;
    }


    public HitBoxOBB(HitBoxAABB_FIXED inHB){
        super(inHB.points, inHB.normals);
        center = calculateCenter(inHB.size);
        extents = calculateExtents(inHB.size, inHB.normals, center);
    }

    @Override
    public HitBoxOBB createWorldHitBox(Matrix4f transformationMatrix, Matrix4f rotationMatrix, Vector3f entityScale, Vector3f entityPosition) {
        List<Vector3f> worldVertex = toWorldSpace(points, transformationMatrix);
        List<Vector3f> worldNormal = toWorldSpace(normals, rotationMatrix)
                .stream().map(g -> (Vector3f) g.normalise()).collect(toList());
        Vector3f worldCenter = toWorldSpace(center, transformationMatrix);
        //extents calculation
        List<Float> worldExtents = calculateWorldExtents(extents, normals, entityScale);
        float worldRadius = rescaleRadius(this.radius, entityScale);
        return new HitBoxOBB(worldVertex, worldNormal, worldCenter, worldExtents, worldRadius);
    }

    public Vector3f getCenter() {
        return center;
    }

    public List<Float> getExtents() {
        return extents;
    }
}
