package renderEngine.loaders.collada;

import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VertexDAE {
    private static final int NO_INDEX = -1;
    private Vector3f position;
    private int textureIndex = -1;
    private int normalIndex = -1;
    private VertexDAE duplicateVertex = null;
    private int index;
    private float length;
    private List<Vector3f> tangents = new ArrayList();
    private Vector3f averagedTangent = new Vector3f(0.0F, 0.0F, 0.0F);
    private VertexSkinData weightsData;

    public VertexDAE(int index, Vector3f position, VertexSkinData weightsData) {
        this.index = index;
        this.weightsData = weightsData;
        this.position = position;
        this.length = position.length();
    }

    public VertexSkinData getWeightsData() {
        return this.weightsData;
    }

    public void addTangent(Vector3f tangent) {
        this.tangents.add(tangent);
    }

    public void averageTangents() {
        if (!this.tangents.isEmpty()) {
            Iterator var2 = this.tangents.iterator();

            while(var2.hasNext()) {
                Vector3f tangent = (Vector3f)var2.next();
                Vector3f.add(this.averagedTangent, tangent, this.averagedTangent);
            }

            this.averagedTangent.normalise();
        }
    }

    public Vector3f getAverageTangent() {
        return this.averagedTangent;
    }

    public int getIndex() {
        return this.index;
    }

    public float getLength() {
        return this.length;
    }

    public boolean isSet() {
        return this.textureIndex != -1 && this.normalIndex != -1;
    }

    public boolean hasSameTextureAndNormal(int textureIndexOther, int normalIndexOther) {
        return textureIndexOther == this.textureIndex && normalIndexOther == this.normalIndex;
    }

    public void setTextureIndex(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public void setNormalIndex(int normalIndex) {
        this.normalIndex = normalIndex;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public int getTextureIndex() {
        return this.textureIndex;
    }

    public int getNormalIndex() {
        return this.normalIndex;
    }

    public VertexDAE getDuplicateVertex() {
        return this.duplicateVertex;
    }

    public void setDuplicateVertex(VertexDAE duplicateVertex) {
        this.duplicateVertex = duplicateVertex;
    }
}
