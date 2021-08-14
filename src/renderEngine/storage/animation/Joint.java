package renderEngine.storage.animation;

import renderEngine.loaders.collada.JointData;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Joint {
    public final int index;
    public final String name;
    public final List<Joint> children = new ArrayList();
    private final Matrix4f localBindTransform;
    private Matrix4f inverseBindTransform = new Matrix4f();
    private Matrix4f animatedTransform = new Matrix4f();

    public Joint(int index, String name, Matrix4f bindLocalTransform) {
        this.index = index;
        this.name = name;
        this.localBindTransform = bindLocalTransform;
    }

    public void addChild(Joint child) {
        this.children.add(child);
    }

    public Matrix4f getInverseBindTransform() {
        return this.inverseBindTransform;
    }

    public Matrix4f getAnimatedTransform() {
        return this.animatedTransform;
    }

    public void setAnimationTransform(Matrix4f animationTransform) {
        this.animatedTransform = animationTransform;
    }

    public void calcInverseBindTransform(Matrix4f parentBindTransform) {
        Matrix4f bindTransform = Matrix4f.mul(parentBindTransform, this.localBindTransform, (Matrix4f)null);
        Matrix4f.invert(bindTransform, this.inverseBindTransform);
        Iterator var4 = this.children.iterator();

        while(var4.hasNext()) {
            Joint child = (Joint)var4.next();
            child.calcInverseBindTransform(bindTransform);
        }

    }

    public static Joint createJointTreeFromJointsData(JointData data) {
        Joint headJoint = new Joint(data.index, data.nameId, data.bindLocalTransform);
        Iterator var3 = data.children.iterator();

        while(var3.hasNext()) {
            JointData child = (JointData)var3.next();
            headJoint.addChild(createJointTreeFromJointsData(child));
        }

        return headJoint;
    }
}
