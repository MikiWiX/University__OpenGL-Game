package renderEngine.storage.animation;

import java.util.Map;

public class KeyFrame {
    private final float timeStamp;
    private final Map<String, JointTransform> pose;

    public KeyFrame(float timeStamp, Map<String, JointTransform> jointKeyFrames) {
        this.timeStamp = timeStamp;
        this.pose = jointKeyFrames;
    }

    protected float getTimeStamp() {
        return this.timeStamp;
    }

    protected Map<String, JointTransform> getJointKeyFrames() {
        return this.pose;
    }
}
