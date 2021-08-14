package renderEngine.loaders.collada;

import org.lwjgl.BufferUtils;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

public class JointsLoader {
    private XmlNode armatureData;
    private List<String> boneOrder;
    private int jointCount = 0;
    private static final Matrix4f CORRECTION = (new Matrix4f()).rotate((float)Math.toRadians(-90.0D), new Vector3f(1.0F, 0.0F, 0.0F));

    public JointsLoader(XmlNode visualSceneNode, List<String> boneOrder) {
        this.armatureData = visualSceneNode.getChild("visual_scene").getChildWithAttribute("node", "id", "Armature");
        this.boneOrder = boneOrder;
    }

    public JointsData extractBoneData() {
        XmlNode headNode = this.armatureData.getChild("node");
        JointData headJoint = this.loadJointData(headNode, true);
        return new JointsData(this.jointCount, headJoint);
    }

    private JointData loadJointData(XmlNode jointNode, boolean isRoot) {
        JointData joint = this.extractMainJointData(jointNode, isRoot);
        Iterator var5 = jointNode.getChildren("node").iterator();

        while(var5.hasNext()) {
            XmlNode childNode = (XmlNode)var5.next();
            joint.addChild(this.loadJointData(childNode, false));
        }

        return joint;
    }

    private JointData extractMainJointData(XmlNode jointNode, boolean isRoot) {
        String nameId = jointNode.getAttribute("id");
        int index = this.boneOrder.indexOf(nameId);
        String[] matrixData = jointNode.getChild("matrix").getData().split(" ");
        Matrix4f matrix = new Matrix4f();
        matrix.load(this.convertData(matrixData));
        matrix.transpose();
        if (isRoot) {
            Matrix4f.mul(CORRECTION, matrix, matrix);
        }

        ++this.jointCount;
        return new JointData(index, nameId, matrix);
    }

    private FloatBuffer convertData(String[] rawData) {
        float[] matrixData = new float[16];

        for(int i = 0; i < matrixData.length; ++i) {
            matrixData[i] = Float.parseFloat(rawData[i]);
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        buffer.put(matrixData);
        buffer.flip();
        return buffer;
    }
}
