package renderEngine.physics.hitBox;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.List;

public class HitBoxAABB_DYNAMIC extends HitBoxAABB_FIXED{

    protected List<Vector3f> srcPoints;

    public HitBoxAABB_DYNAMIC(List<Vector3f> points){
        super(points);
        this.srcPoints = points;
    }

    public HitBoxAABB_DYNAMIC(List<Vector3f> points, float radius){
        super(points, radius);
        this.srcPoints = points;
    }

    @Override
    public HitBoxAABB_DYNAMIC createWorldHitBox(Matrix4f transformationMatrix, Matrix4f rotationMatrix, Vector3f entityScale, Vector3f entityPosition) {
        List<Vector3f> worldVertex = toWorldSpace(srcPoints, transformationMatrix);
        float worldRadius = rescaleRadius(this.radius, entityScale);
        return new HitBoxAABB_DYNAMIC(worldVertex, worldRadius);
    }

    public List<Vector3f> getSrcPoints() {
        return srcPoints;
    }

}
