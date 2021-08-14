package renderEngine.loaders.collada;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.storage.RawOBJModel;
import renderEngine.storage.animation.Animation;
import renderEngine.storage.animation.JointTransform;
import renderEngine.storage.animation.KeyFrame;
import renderEngine.toolbox.Quaternion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ColladaLoader {
    public ColladaLoader() {
    }

    public static RawOBJModel loadColladaModel(String colladaFile, int maxWeights) {
        XmlNode node = XmlParser.loadXmlFile(colladaFile);
        SkinLoader skinLoader = new SkinLoader(node.getChild("library_controllers"), maxWeights);
        SkinningData skinningData = skinLoader.extractSkinData();
        JointsLoader jointsLoader = new JointsLoader(node.getChild("library_visual_scenes"), skinningData.jointOrder);
        JointsData jointsData = jointsLoader.extractBoneData();
        GeometryLoader g = new GeometryLoader(node.getChild("library_geometries"), skinningData.verticesSkinData);
        RawOBJModel meshData = g.extractModelData();
        meshData.setJointsData(jointsData);
        return meshData;
    }

    public static Animation loadAnimation(String colladaFile) {
        AnimationData animationData = loadColladaAnimation(colladaFile);
        KeyFrame[] frames = new KeyFrame[animationData.keyFrames.length];

        for(int i = 0; i < frames.length; ++i) {
            frames[i] = createKeyFrame(animationData.keyFrames[i]);
        }

        return new Animation(animationData.lengthSeconds, frames);
    }

    private static KeyFrame createKeyFrame(KeyFrameData data) {
        Map<String, JointTransform> map = new HashMap<>();
        Iterator var3 = data.jointTransforms.iterator();

        while(var3.hasNext()) {
            JointTransformData jointData = (JointTransformData)var3.next();
            JointTransform jointTransform = createTransform(jointData);
            map.put(jointData.jointNameId, jointTransform);
        }

        return new KeyFrame(data.time, map);
    }

    private static JointTransform createTransform(JointTransformData data) {
        Matrix4f mat = data.jointLocalTransform;
        Vector3f translation = new Vector3f(mat.m30, mat.m31, mat.m32);
        Quaternion rotation = new Quaternion(mat);
        return new JointTransform(translation, rotation);
    }

    public static AnimationData loadColladaAnimation(String colladaFile) {
        XmlNode node = XmlParser.loadXmlFile(colladaFile);
        XmlNode animNode = node.getChild("library_animations");
        XmlNode jointsNode = node.getChild("library_visual_scenes");
        AnimationLoader loader = new AnimationLoader(animNode, jointsNode);
        AnimationData animData = loader.extractAnimation();
        return animData;
    }
}