package renderEngine.entities.entityComponents;

import renderEngine.storage.animation.Animation;
import renderEngine.storage.animation.Animator;
import renderEngine.storage.animation.Joint;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;

import java.util.Iterator;

public class CmpAnimation extends CmpInterface{

    private Animator animator;
    //private List<Animation> animationData;

    public CmpAnimation() {
        //this.animationData = Collections.singletonList(animation);
        //this.animationData = animation;
    }
    public CmpAnimation(Animation animation) {
        this();
        doAnimation(animation);
    }

    @Override
    public void init() {
        this.animator = new Animator(this.entity.getModel());
    }

    @Override
    public void update() {
        animator.update();
    }

    public void doAnimation(Animation animation){
        this.animator.doAnimation(animation);
    }

    public Matrix4f[] getJointTransforms() {
        Matrix4f[] jointMatrices = new Matrix4f[entity.getModel().getJointCount()];
        this.addJointsToArray(entity.getModel().getRootJoint(), jointMatrices);
        return jointMatrices;
    }

    private void addJointsToArray(Joint headJoint, Matrix4f[] jointMatrices) {
        jointMatrices[headJoint.index] = headJoint.getAnimatedTransform();
        Iterator var4 = headJoint.children.iterator();

        while(var4.hasNext()) {
            Joint childJoint = (Joint)var4.next();
            this.addJointsToArray(childJoint, jointMatrices);
        }
    }
}
