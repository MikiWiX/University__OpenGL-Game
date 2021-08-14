package renderEngine.physics.hitBox;

import renderEngine.physics.CollisionDetector;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;

public class HitBoxHULL{

    protected List<Vector3f> points;
    protected List<Vector3f> normals;

    protected float radius;

    // float{minX, maxX}, float{minY, maxY}, float{minZ, maxZ}
    protected List<float[]> selfSATTest;

    public HitBoxHULL(List<Vector3f> points, List<Vector3f> normals) {
        this.points = points;
        this.normals = normals;
        this.radius = calculateRadius(points);
    }

    public HitBoxHULL(List<Vector3f> points, List<Vector3f> normals, float radius) {
        this.points = points;
        this.normals = normals;
        this.radius = radius;
    }

    public HitBoxHULL createWorldHitBox(Matrix4f transformationMatrix, Matrix4f rotationMatrix, Vector3f entityScale, Vector3f entityPosition) {
        List<Vector3f> worldVertex = toWorldSpace(points, transformationMatrix);
        List<Vector3f> worldNormal = toWorldSpace(normals, rotationMatrix)
                .stream().map(g -> (Vector3f) g.normalise()).collect(toList());
        float worldRadius = rescaleRadius(this.radius, entityScale);
        return new HitBoxHULL(worldVertex, worldNormal, worldRadius);
    }

    public static List<Vector3f> pointsFromSize(List<Vector3f> localSize){
        return Arrays.asList(
                new Vector3f( localSize.get(0).x, localSize.get(0).y, localSize.get(0).z),
                new Vector3f( localSize.get(0).x, localSize.get(0).y, localSize.get(1).z),
                new Vector3f( localSize.get(0).x, localSize.get(1).y, localSize.get(0).z),
                new Vector3f( localSize.get(0).x, localSize.get(1).y, localSize.get(1).z),
                new Vector3f( localSize.get(1).x, localSize.get(0).y, localSize.get(0).z),
                new Vector3f( localSize.get(1).x, localSize.get(0).y, localSize.get(1).z),
                new Vector3f( localSize.get(1).x, localSize.get(1).y, localSize.get(0).z),
                new Vector3f( localSize.get(1).x, localSize.get(1).y, localSize.get(1).z)
        );
        /*IntStream.range(0, 1).forEachOrdered(i ->
                IntStream.range(0, 1).forEachOrdered(j ->
                        IntStream.range(0, 1).forEachOrdered(k ->
                                localPoints.add(new Vector3f( localSize.get(i).x, localSize.get(j).y, localSize.get(k).z))
                        )
                )
        );*/
    }

    public static List<Vector3f> calculateSize(List<Vector3f> localPoints){
        float maxX = -HUGE, minX = HUGE,
                maxY = -HUGE, minY = HUGE,
                maxZ = -HUGE, minZ = HUGE;
        for (Vector3f vertex : localPoints){
            maxX = Math.max(vertex.x, maxX);
            minX = Math.min(vertex.x, minX);

            maxY = Math.max(vertex.y, maxY);
            minY = Math.min(vertex.y, minY);

            maxZ = Math.max(vertex.z, maxZ);
            minZ = Math.min(vertex.z, minZ);
        }
        List<Vector3f> localSize = new ArrayList<>();
        localSize.add(new Vector3f(minX, minY, minZ));
        localSize.add(new Vector3f(maxX, maxY, maxZ));
        return localSize;
    }

    private static float HUGE = Float.MAX_VALUE -1;

    public static Vector3f calculateCenter(List<Vector3f> localSize){
        float avgX = (localSize.get(0).x + localSize.get(1).x)/2;
        float avgY = (localSize.get(0).y + localSize.get(1).y)/2;
        float avgZ = (localSize.get(0).z + localSize.get(1).z)/2;
        return new Vector3f(avgX, avgY, avgZ);
    }

    public static List<Float> calculateExtents(List<Vector3f> localPoints, List<Vector3f> localNormals, Vector3f localCenter) {
        float vec;
        float centerDot;
        List<Float> localExtents = new ArrayList<>();

        for (Vector3f normal : localNormals){

            centerDot = Vector3f.dot(normal, localCenter);
            float min = HUGE, max = -HUGE;

            for (Vector3f vertex : localPoints){
                vec = Vector3f.dot(normal, vertex);
                if (vec>max){
                    max = vec;
                } else if(vec<min){
                    min = vec;
                }
            }

            //these two values are ALMOST equal, both same extent but to opposite wall
            float ax = abs(centerDot-max);
            //float ay = abs(centerDot-min);

            localExtents.add(ax);
        }
        return localExtents;
    }

    public List<float[]> getOwnWorldSATTest() {
        //remember own SAT scores (no reason to re-calculate it for every collision)
        if(selfSATTest == null) {
            selfSATTest = new ArrayList<>();
            for(Vector3f normal : normals){
                selfSATTest.add(CollisionDetector.SATTest(normal, points));
            }
        }
        return selfSATTest;
    }

    public static float calculateRadius(List<Vector3f> points) {
        float localRadius = 0;
        for (Vector3f point : points){
            float l = point.length();
            if(l>localRadius){
                localRadius = l;
            }
        }
        return (float) (localRadius * 1.05);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    protected static List<Vector3f> toWorldSpace(List<Vector3f> vertices, Vector3f position, Vector3f scale){
        List<Vector3f> worldVertices = new ArrayList<>();
        for(Vector3f vertex : vertices) {
            Vector3f rescaledVertex = new Vector3f(vertex.x*scale.x, vertex.y*scale.y,vertex.z*scale.z);
            worldVertices.add(Vector3f.add(rescaledVertex, position, null));
        }
        return worldVertices;
    }
    protected static List<Vector3f> toWorldSpace(List<Vector3f> vertices, Matrix4f transformationMatrix){
        List<Vector3f> worldVertices = new ArrayList<>();
        for(Vector3f vertex : vertices) {
            worldVertices.add(toWorldSpace(vertex, transformationMatrix));
        }
        return worldVertices;
    }
    protected static Vector3f toWorldSpace(Vector3f vertex, Matrix4f transformationMatrix){
        Vector4f tmp = Matrix4f.transform(transformationMatrix, new Vector4f(vertex.x, vertex.y, vertex.z, 1), null);
        return new Vector3f(tmp.x, tmp.y, tmp.z);
    }

    protected List<Float> calculateWorldExtents(List<Float> srcExtents, List<Vector3f> srcAxis, Vector3f sca) {
        List<Float> worldExtents = new ArrayList<>();
        float outX, outY, outZ, ext;
        for (int i=0; i<3; i++){
            outX = srcAxis.get(i).getX()*sca.getX();
            outY = srcAxis.get(i).getY()*sca.getY();
            outZ = srcAxis.get(i).getZ()*sca.getZ();
            ext = new Vector3f(outX, outY, outZ).length() * srcExtents.get(i);
            worldExtents.add(ext);
        }
        return worldExtents;
    }

    protected float rescaleRadius(float srcRadius, Vector3f scale){
        return srcRadius * Math.max(Math.max(scale.x, scale.y), scale.z);
    }

    public List<Vector3f> getPoints() {
        return points;
    }

    public List<Vector3f> getNormals() {
        return normals;
    }

    public Vector3f getCenter() {
        return calculateCenter(calculateSize(points));
    }
}
