package renderEngine.storage.animation;

import renderEngine.GameMain;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.storage.Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Animator {
    private final Model model;
    private float animationTime = 0.0F;
    private Animation currentAnimation;

    public Animator(Model model) {
        this.model = model;
    }

    public void doAnimation(Animation animation) {
        this.animationTime = 0.0F;
        this.currentAnimation = animation;
    }

    public void update() {
        if (this.currentAnimation != null) {
            this.increaseAnimationTime(GameMain.getFrameRenderTime());
            Map<String, Matrix4f> currentPose = this.calculateCurrentAnimationPose();
            this.applyPoseToJoints(currentPose, this.model.getRootJoint(), new Matrix4f());
        }
    }

    private void increaseAnimationTime(float time) {
        this.animationTime += time;
        if (this.animationTime > this.currentAnimation.getLength()) {
            this.animationTime %= this.currentAnimation.getLength();
        }

    }

    private Map<String, Matrix4f> calculateCurrentAnimationPose() {
        KeyFrame[] frames = this.getPreviousAndNextFrames();
        float progression = this.calculateProgression(frames[0], frames[1]);
        return this.interpolatePoses(frames[0], frames[1], progression);
    }

    private void applyPoseToJoints(Map<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
        Matrix4f currentLocalTransform = (Matrix4f)currentPose.get(joint.name);
        Matrix4f currentTransform = Matrix4f.mul(parentTransform, currentLocalTransform, (Matrix4f)null);
        Iterator var7 = joint.children.iterator();

        while(var7.hasNext()) {
            Joint childJoint = (Joint)var7.next();
            this.applyPoseToJoints(currentPose, childJoint, currentTransform);
        }

        Matrix4f.mul(currentTransform, joint.getInverseBindTransform(), currentTransform);
        joint.setAnimationTransform(currentTransform);
    }

    private KeyFrame[] getPreviousAndNextFrames() {
        KeyFrame previousFrame = null;
        KeyFrame nextFrame = null;
        KeyFrame[] var6;
        int var5 = (var6 = this.currentAnimation.getKeyFrames()).length;

        for(int var4 = 0; var4 < var5; ++var4) {
            KeyFrame frame = var6[var4];
            if (frame.getTimeStamp() > this.animationTime) {
                nextFrame = frame;
                break;
            }

            previousFrame = frame;
        }

        previousFrame = previousFrame == null ? nextFrame : previousFrame;
        nextFrame = nextFrame == null ? previousFrame : nextFrame;
        return new KeyFrame[]{previousFrame, nextFrame};
    }

    private float calculateProgression(KeyFrame previousFrame, KeyFrame nextFrame) {
        float timeDifference = nextFrame.getTimeStamp() - previousFrame.getTimeStamp();
        return (this.animationTime - previousFrame.getTimeStamp()) / timeDifference;
    }

    private Map<String, Matrix4f> interpolatePoses(KeyFrame previousFrame, KeyFrame nextFrame, float progression) {
        Map<String, Matrix4f> currentPose = new HashMap();
        Iterator var6 = previousFrame.getJointKeyFrames().keySet().iterator();

        while(var6.hasNext()) {
            String jointName = (String)var6.next();
            JointTransform previousTransform = (JointTransform)previousFrame.getJointKeyFrames().get(jointName);
            JointTransform nextTransform = (JointTransform)nextFrame.getJointKeyFrames().get(jointName);
            JointTransform currentTransform = JointTransform.interpolate(previousTransform, nextTransform, progression);
            currentPose.put(jointName, currentTransform.getLocalTransform());
        }

        return currentPose;
    }
}
