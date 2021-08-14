package renderEngine.loaders.collada;

import java.util.ArrayList;
import java.util.List;

public class VertexSkinData {
    public final List<Integer> jointIds = new ArrayList();
    public final List<Float> weights = new ArrayList();

    public VertexSkinData() {
    }

    protected void addJointEffect(int jointId, float weight) {
        for(int i = 0; i < this.weights.size(); ++i) {
            if (weight > (Float)this.weights.get(i)) {
                this.jointIds.add(i, jointId);
                this.weights.add(i, weight);
                return;
            }
        }

        this.jointIds.add(jointId);
        this.weights.add(weight);
    }

    protected void limitJointNumber(int max) {
        if (this.jointIds.size() > max) {
            float[] topWeights = new float[max];
            float total = this.saveTopWeights(topWeights);
            this.refillWeightList(topWeights, total);
            this.removeExcessJointIds(max);
        } else if (this.jointIds.size() < max) {
            this.fillEmptyWeights(max);
        }

    }

    private void fillEmptyWeights(int max) {
        while(this.jointIds.size() < max) {
            this.jointIds.add(0);
            this.weights.add(0.0F);
        }

    }

    private float saveTopWeights(float[] topWeightsArray) {
        float total = 0.0F;

        for(int i = 0; i < topWeightsArray.length; ++i) {
            topWeightsArray[i] = (Float)this.weights.get(i);
            total += topWeightsArray[i];
        }

        return total;
    }

    private void refillWeightList(float[] topWeights, float total) {
        this.weights.clear();

        for(int i = 0; i < topWeights.length; ++i) {
            this.weights.add(Math.min(topWeights[i] / total, 1.0F));
        }

    }

    private void removeExcessJointIds(int max) {
        while(this.jointIds.size() > max) {
            this.jointIds.remove(this.jointIds.size() - 1);
        }

    }
}
