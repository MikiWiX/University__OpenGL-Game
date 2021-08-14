package renderEngine.physics;

import renderEngine.entities.Entity;
import renderEngine.entities.entityComponents.CmpHitBox;
import renderEngine.physics.hitBox.*;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.List;


public class CollisionDetector {

    public static boolean intersectionTest(Entity entity1, Entity entity2) {

        HitBoxHULL hb1;
        HitBoxHULL hb2;
        try {
            hb1 = entity1.getComponent(CmpHitBox.class).getWorldSpaceHitBox();
            hb2 = entity2.getComponent(CmpHitBox.class).getWorldSpaceHitBox();
        } catch (NullPointerException e) {
            return false;
        }

        Class<? extends HitBoxHULL> hbc1 = hb1.getClass();
        Class<? extends HitBoxHULL> hbc2 = hb2.getClass();

        if( ( hbc1 == HitBoxAABB_DYNAMIC.class || hbc1 == HitBoxAABB_FIXED.class ) && ( hbc2 == HitBoxAABB_DYNAMIC.class || hbc2 == HitBoxAABB_FIXED.class ) ) {

            return testAABBvsAABB((HitBoxAABB_FIXED) hb1, (HitBoxAABB_FIXED) hb2);

        } else if ( quickRadiusDistanceTest(hb1, hb2, entity1.getPosition(), entity2.getPosition()) ) {

            if ( hbc1 == HitBoxHULL.class  ||  hbc2 == HitBoxHULL.class ) {

                return testHulls(hb1, hb2);

            } else {

                if( hbc1 == HitBoxAABB_DYNAMIC.class || hbc1 == HitBoxAABB_FIXED.class ){
                    hb1 = ((HitBoxAABB_FIXED) hb1).getAsOBB();
                }
                if( hbc2 == HitBoxAABB_DYNAMIC.class || hbc2 == HitBoxAABB_FIXED.class ){
                    hb2 = ((HitBoxAABB_FIXED) hb2).getAsOBB();
                }

                return testOBBvsOBB((HitBoxOBB) hb1, (HitBoxOBB) hb2 );
            }
        }
        return false;
    }

    private static boolean quickRadiusDistanceTest(HitBoxHULL hb1, HitBoxHULL hb2, Vector3f entityPos1, Vector3f entityPos2) {
        float reqDistance = hb1.getRadius() + hb2.getRadius();
        float disX = Math.abs(entityPos1.x - entityPos2.x);
        float disY = Math.abs(entityPos1.x - entityPos2.x);
        float disZ = Math.abs(entityPos1.x - entityPos2.x);
        return reqDistance > disX || reqDistance > disY || reqDistance > disZ;
    }

    private static boolean testAABBvsAABB(HitBoxAABB_FIXED hb1, HitBoxAABB_FIXED hb2) {

        //size of a AABB hit box are 2 opposite points of cuboid
        List<Vector3f> sA = hb1.getSize();
        List<Vector3f> sB = hb2.getSize();

        if(notOverlaps( sA.get(0).x, sA.get(1).x, sB.get(0).x, sB.get(1).x )){
            return false;
        }
        if(notOverlaps( sA.get(0).y, sA.get(1).y, sB.get(0).y, sB.get(1).y )){
            return false;
        }
        if(notOverlaps( sA.get(0).z, sA.get(1).z, sB.get(0).z, sB.get(1).z )){
            return false;
        }

        return true;
    }

    /**
     * What this does is basically a SAT ( looking for non-intersection axis/plane ) test,
     * but mathematically simplified, which means faster.
     * Works only with CUBOIDS.
     * More details about SAT in function below this one.
     * REQUIRED DATA: hitBox normals/axis, extents(how far from center are walls) and center point
     * @param hb1 input hitBox1 in worldSpace
     * @param hb2 input hitBox2 in worldSpace
     * @return bool if objects collide
     */
    private static boolean testOBBvsOBB(HitBoxOBB hb1, HitBoxOBB hb2) {

        List<Vector3f> axisA = hb1.getNormals();
        List<Vector3f> axisB = hb2.getNormals();

        List<Float> a = hb1.getExtents();
        List<Float> b = hb2.getExtents();

        float[][] c = {
                { Vector3f.dot(axisA.get(0), axisB.get(0)), Vector3f.dot(axisA.get(0), axisB.get(1)), Vector3f.dot(axisA.get(0), axisB.get(2)) },
                { Vector3f.dot(axisA.get(1), axisB.get(0)), Vector3f.dot(axisA.get(1), axisB.get(1)), Vector3f.dot(axisA.get(1), axisB.get(2)) },
                { Vector3f.dot(axisA.get(2), axisB.get(0)), Vector3f.dot(axisA.get(2), axisB.get(1)), Vector3f.dot(axisA.get(2), axisB.get(2)) }
        };

        float[][] absC = {
                { Math.abs(c[0][0]), Math.abs(c[0][1]), Math.abs(c[0][2])},
                { Math.abs(c[1][0]), Math.abs(c[1][1]), Math.abs(c[1][2])},
                { Math.abs(c[2][0]), Math.abs(c[2][1]), Math.abs(c[2][2])}
        };

        Vector3f d = Vector3f.sub(hb2.getCenter(), hb1.getCenter(), null);

        float r0, r1, r; // NON-INTERSECTION TEST , passed if R > R0+R1
        for (int i=0; i < 3; i++) { // for axisA

            r0 = a.get(i);
            r1 = ( b.get(0) * absC[i][0] ) + ( b.get(1) * absC[i][1] ) + ( b.get(2) * absC[i][2] );
            r = Math.abs(Vector3f.dot(axisA.get(i), d));

            if(r > r0+r1){
                return false;
            }
        }
        for (int i=0; i < 3; i++) { // for axisB

            r0 = ( a.get(0) * absC[0][i] ) + ( a.get(1) * absC[1][i] ) + ( a.get(2) * absC[2][i] );
            r1 = b.get(i);
            r = Math.abs(Vector3f.dot(axisB.get(i), d));

            if(r > r0+r1){
                return false;
            }
        }

        int r0_i0, r0_i1, //indexes required for R0 calculations (change with i)
                r1_i0, r1_i1, //indexes required for R1 calculations (change with j)
                r_i0, r_i1; //indexes required for R calculations (change with i)
        float r_p0, r_p1; // just two parts of R, easier to keep track of them if calculated separately
        for (int i=0; i < 3; i++) { // for axisA x axisB

            r0_i0 = (i==0)? 1 : 0;
            r0_i1 = (i==2)? 1 : 2;

            r_i0 = (i!=2)? i+1 : 0;
            r_i1 = (i!=0)? i-1 : 2;

            for (int j=0; j < 3; j++) {
                //Vector3f axis = Vector3f.cross(axisA.get(i), axisB.get(j), null);

                r1_i0 = (j==0)? 1 : 0;
                r1_i1 = (j==2)? 1 : 2;

                r0 = ( a.get(r0_i0) * absC[r0_i1][j] ) + ( a.get(r0_i1) * absC[r0_i0][j] );
                r1 = ( b.get(r1_i0) * absC[i][r1_i1] ) + ( b.get(r1_i1) * absC[i][r1_i0] );

                r_p0 = Vector3f.dot(axisA.get(r_i1),d) * c[r_i0][j];
                r_p1 = Vector3f.dot(axisA.get(r_i0),d) * c[r_i1][j];

                r = Math.abs( r_p0 - r_p1 );

                if(r > r0+r1){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Simplest (logically, not for computing) possible non-intersection test (SAT test), works with any hull.
     * Loops through all possible axis and try to find at least one at which no overlap occurs.
     * REQUIRED DATA: hitBox vertices and normals/axis
     * @param hb1 input hitBox1 in worldSpace
     * @param hb2 input hitBox2 in worldSpace
     * @return bool if objects collide
     */
    private static boolean testHulls(HitBoxHULL hb1, HitBoxHULL hb2) {

        for (int i = 0; i < hb1.getNormals().size(); i++) {
            float[] sh1, sh2;
            //sh1 = selfSATTest1.get(i);
            sh1 = hb1.getOwnWorldSATTest().get(i);
            sh2 = SATTest(hb1.getNormals().get(i), hb2.getPoints());
            if (notOverlaps(sh1, sh2)) { // NON-INTERSECTION TEST , passed if no overlap detected
                return false;
            }
        }
        for (int i = 0; i < hb2.getNormals().size(); i++) {
            float[] sh1, sh2;
            sh1 = SATTest(hb2.getNormals().get(i), hb1.getPoints());
            //sh2 = selfSATTest2.get(i);
            sh2 = hb2.getOwnWorldSATTest().get(i);
            if (notOverlaps(sh1, sh2)) { // NON-INTERSECTION TEST , passed if no overlap detected
                return false;
            }
        }
        for (Vector3f n1 : hb1.getNormals()) {
            for (Vector3f n2 : hb2.getNormals()) {
                if (!(n1.x == n2.x && n1.y == n2.y && n1.z == n2.z || n1.x == -n2.x && n1.y == -n2.y && n1.z == -n2.z)) {
                    Vector3f vec = Vector3f.cross(n1, n2, null); //axis A.dot(B) also might be non-intersection one
                    vec.normalise();
                    float[] sh1, sh2;
                    sh1 = SATTest(vec, hb1.getPoints());
                    sh2 = SATTest(vec, hb2.getPoints());
                    if (notOverlaps(sh1, sh2)) { // NON-INTERSECTION TEST , passed if no overlap detected
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static float[] SATTest(Vector3f axis, List<Vector3f> pointSet){
        float HUGE = Float.MAX_VALUE - 1;

        float maxPoint = -HUGE, minPoint = HUGE;
        for (Vector3f vector3f : pointSet) {

            float dotVal = Vector3f.dot(vector3f, axis);
            //if axis is not normalised

            /*float ax2 = axis.lengthSquared();
            float projX = dotVal * axis.x;
            float projY = dotVal * axis.y;
            float projZ = dotVal * axis.z;
            float dotVal = new Vector3f(projX, projY, projZ).lengthSquared();*/

            if (dotVal < minPoint) {
                minPoint = dotVal;
            }
            if (dotVal > maxPoint) {
                maxPoint = dotVal;
            }
        }
        return new float[]{minPoint, maxPoint};
    }

    private static boolean overlaps(float sh1min, float sh1max, float sh2min, float sh2max){
        return sh1max > sh2min && sh2max > sh1min;
    }
    private static boolean overlaps(float[] sh1, float[] sh2){
        return sh1[1] > sh2[0] && sh2[1] > sh1[0];
    }
    private static boolean notOverlaps(float sh1min, float sh1max, float sh2min, float sh2max){
        //return isBetweenOrdered(sh2min, sh1min, sh1max) || isBetweenOrdered(sh1min, sh2min, sh2max);
        return sh1max < sh2min || sh2max < sh1min;
    }
    private static boolean notOverlaps(float[] sh1, float[] sh2){
        //return isBetweenOrdered(sh2[0], sh1[0], sh1[1]) || isBetweenOrdered(sh1[0], sh2[0], sh2[1]);
        return sh1[1] < sh2[0] || sh2[1] < sh1[0];
    }

    /*private static boolean isBetweenOrdered(float val, float lowerBound, float upperBound){
        return lowerBound <= val && val <= upperBound;
    }*/

}
