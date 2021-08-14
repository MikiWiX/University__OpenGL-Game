package renderEngine.loaders.collada;

import org.lwjgl.BufferUtils;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

public class AnimationLoader {
    private static final Matrix4f CORRECTION = (new Matrix4f()).rotate((float)Math.toRadians(-90.0D), new Vector3f(1.0F, 0.0F, 0.0F));
    private XmlNode animationData;
    private XmlNode jointHierarchy;

    public AnimationLoader(XmlNode animationData, XmlNode jointHierarchy) {
        this.animationData = animationData;
        this.jointHierarchy = jointHierarchy;
    }

    public AnimationData extractAnimation() {
        String rootNode = this.findRootJointName();
        float[] times = this.getKeyTimes();
        float duration = times[times.length - 1];
        KeyFrameData[] keyFrames = this.initKeyFrames(times);
        List<XmlNode> animationNodes = this.animationData.getChildren("animation");
        Iterator var7 = animationNodes.iterator();

        while(var7.hasNext()) {
            XmlNode jointNode = (XmlNode)var7.next();
            this.loadJointTransforms(keyFrames, jointNode, rootNode);
        }

        return new AnimationData(duration, keyFrames);
    }

    private float[] getKeyTimes() {
        XmlNode timeData = this.animationData.getChild("animation").getChild("source").getChild("float_array");
        String[] rawTimes = timeData.getData().split(" ");
        float[] times = new float[rawTimes.length];

        for(int i = 0; i < times.length; ++i) {
            times[i] = Float.parseFloat(rawTimes[i]);
        }

        return times;
    }

    private KeyFrameData[] initKeyFrames(float[] times) {
        KeyFrameData[] frames = new KeyFrameData[times.length];

        for(int i = 0; i < frames.length; ++i) {
            frames[i] = new KeyFrameData(times[i]);
        }

        return frames;
    }

    private void loadJointTransforms(KeyFrameData[] frames, XmlNode jointData, String rootNodeId) {
        String jointNameId = this.getJointName(jointData);
        String dataId = this.getDataId(jointData);
        XmlNode transformData = jointData.getChildWithAttribute("source", "id", dataId);
        String[] rawData = transformData.getChild("float_array").getData().split(" ");
        this.processTransforms(jointNameId, rawData, frames, jointNameId.equals(rootNodeId));
    }

    private String getDataId(XmlNode jointData) {
        XmlNode node = jointData.getChild("sampler").getChildWithAttribute("input", "semantic", "OUTPUT");
        return node.getAttribute("source").substring(1);
    }

    private String getJointName(XmlNode jointData) {
        XmlNode channelNode = jointData.getChild("channel");
        String data = channelNode.getAttribute("target");
        return data.split("/")[0];
    }

    private void processTransforms(String jointName, String[] rawData, KeyFrameData[] keyFrames, boolean root) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        float[] matrixData = new float[16];

        for(int i = 0; i < keyFrames.length; ++i) {
            for(int j = 0; j < 16; ++j) {
                matrixData[j] = Float.parseFloat(rawData[i * 16 + j]);
            }

            buffer.clear();
            buffer.put(matrixData);
            buffer.flip();
            Matrix4f transform = new Matrix4f();
            transform.load(buffer);
            transform.transpose();
            if (root) {
                Matrix4f.mul(CORRECTION, transform, transform);
            }

            keyFrames[i].addJointTransform(new JointTransformData(jointName, transform));
        }

    }

    private String findRootJointName() {
        XmlNode skeleton = this.jointHierarchy.getChild("visual_scene").getChildWithAttribute("node", "id", "Armature");
        return skeleton.getChild("node").getAttribute("id");
    }
}
