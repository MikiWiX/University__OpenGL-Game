package renderEngine.storage;

import renderEngine.loaders.collada.JointsData;

public class RawOBJModel {

    private float[] vertices;
    private float[] textureCoords;
    private float[] normals;
    private float[] tangents;
    private int[] indices;
    private float furthestPoint;
    //if animated
    private boolean isAnimated = false;
    private static final int DIMENSIONS = 3;
    private int[] jointIds;
    private float[] vertexWeights;
    private JointsData joints;

    public RawOBJModel(float[] vertices, float[] textureCoords, float[] normals, int[] indices, float[] tangents, float furthestPoint) {
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.indices = indices;
        this.furthestPoint = furthestPoint;
        this.tangents = tangents;
    }

    // for animated models
    public RawOBJModel(float[] vertices, float[] textureCoords, float[] normals, int[] indices, float[] tangents, float furthestPoint,
                       int[] jointIds, float[] vertexWeights) {
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.indices = indices;
        this.furthestPoint = furthestPoint;
        this.tangents = tangents;
        this.jointIds = jointIds;
        this.vertexWeights = vertexWeights;
        this.isAnimated = true;
    }

    public void setJointsData(JointsData joints) {
        this.joints = joints;
    }

    public JointsData getJointsData() {
        return joints;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getJointIds() {
        return this.jointIds;
    }

    public float[] getVertexWeights() {
        return this.vertexWeights;
    }

    public int getVertexCount() {
        return this.vertices.length / 3;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public float[] getTangents(){
        return tangents;
    }

    public float[] getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }

    public float getFurthestPoint() {
        return furthestPoint;
    }

    public boolean getIsAnimated() {
        return isAnimated;
    }

}
