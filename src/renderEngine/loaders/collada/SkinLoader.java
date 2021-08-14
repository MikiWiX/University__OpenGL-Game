package renderEngine.loaders.collada;

import java.util.ArrayList;
import java.util.List;

public class SkinLoader {
    private final XmlNode skinningData;
    private final int maxWeights;

    public SkinLoader(XmlNode controllersNode, int maxWeights) {
        this.skinningData = controllersNode.getChild("controller").getChild("skin");
        this.maxWeights = maxWeights;
    }

    public SkinningData extractSkinData() {
        List<String> jointsList = this.loadJointsList();
        float[] weights = this.loadWeights();
        XmlNode weightsDataNode = this.skinningData.getChild("vertex_weights");
        int[] effectorJointCounts = this.getEffectiveJointsCounts(weightsDataNode);
        List<VertexSkinData> vertexWeights = this.getSkinData(weightsDataNode, effectorJointCounts, weights);
        return new SkinningData(jointsList, vertexWeights);
    }

    private List<String> loadJointsList() {
        XmlNode inputNode = this.skinningData.getChild("vertex_weights");
        String jointDataId = inputNode.getChildWithAttribute("input", "semantic", "JOINT").getAttribute("source").substring(1);
        XmlNode jointsNode = this.skinningData.getChildWithAttribute("source", "id", jointDataId).getChild("Name_array");
        String[] names = jointsNode.getData().split(" ");
        List<String> jointsList = new ArrayList();
        String[] var9 = names;
        int var8 = names.length;

        for(int var7 = 0; var7 < var8; ++var7) {
            String name = var9[var7];
            jointsList.add(name);
        }

        return jointsList;
    }

    private float[] loadWeights() {
        XmlNode inputNode = this.skinningData.getChild("vertex_weights");
        String weightsDataId = inputNode.getChildWithAttribute("input", "semantic", "WEIGHT").getAttribute("source").substring(1);
        XmlNode weightsNode = this.skinningData.getChildWithAttribute("source", "id", weightsDataId).getChild("float_array");
        String[] rawData = weightsNode.getData().split(" ");
        float[] weights = new float[rawData.length];

        for(int i = 0; i < weights.length; ++i) {
            weights[i] = Float.parseFloat(rawData[i]);
        }

        return weights;
    }

    private int[] getEffectiveJointsCounts(XmlNode weightsDataNode) {
        String[] rawData = weightsDataNode.getChild("vcount").getData().split(" ");
        int[] counts = new int[rawData.length];

        for(int i = 0; i < rawData.length; ++i) {
            counts[i] = Integer.parseInt(rawData[i]);
        }

        return counts;
    }

    private List<VertexSkinData> getSkinData(XmlNode weightsDataNode, int[] counts, float[] weights) {
        String[] rawData = weightsDataNode.getChild("v").getData().split(" ");
        List<VertexSkinData> skinningData = new ArrayList();
        int pointer = 0;
        int[] var10 = counts;
        int var9 = counts.length;

        for(int var8 = 0; var8 < var9; ++var8) {
            int count = var10[var8];
            VertexSkinData skinData = new VertexSkinData();

            for(int i = 0; i < count; ++i) {
                int jointId = Integer.parseInt(rawData[pointer++]);
                int weightId = Integer.parseInt(rawData[pointer++]);
                skinData.addJointEffect(jointId, weights[weightId]);
            }

            skinData.limitJointNumber(this.maxWeights);
            skinningData.add(skinData);
        }

        return skinningData;
    }
}
