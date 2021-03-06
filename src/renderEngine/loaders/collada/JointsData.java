package renderEngine.loaders.collada;

public class JointsData {
    public final int jointCount;
    public final JointData headJoint;

    protected JointsData(int jointCount, JointData headJoint) {
        this.jointCount = jointCount;
        this.headJoint = headJoint;
    }
}
