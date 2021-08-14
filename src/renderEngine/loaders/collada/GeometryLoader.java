package renderEngine.loaders.collada;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector4f;
import renderEngine.storage.RawOBJModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GeometryLoader {
    private final XmlNode meshData;
    private final List<VertexSkinData> vertexWeights;
    private float[] verticesArray;
    private float[] normalsArray;
    private float[] texturesArray;
    private float[] tangentsArray;
    private int[] indicesArray;
    private int[] jointIdsArray;
    private float[] weightsArray;
    private float furthestPoint;
    List<VertexDAE> vertices = new ArrayList();
    List<Vector2f> textures = new ArrayList();
    List<Vector3f> normals = new ArrayList();
    List<Integer> indices = new ArrayList();
    private Matrix4f correction = (new Matrix4f()).rotate((float)Math.toRadians(-90.0D), new Vector3f(1.0F, 0.0F, 0.0F));

    public GeometryLoader(XmlNode geometryNode, List<VertexSkinData> vertexWeights) {
        this.vertexWeights = vertexWeights;
        this.meshData = geometryNode.getChild("geometry").getChild("mesh");
    }

    public RawOBJModel extractModelData() {
        this.readRawData();
        this.assembleVertices();
        this.removeUnusedVertices();
        this.initArrays();
        this.convertDataToArrays();
        this.convertIndicesListToArray();
        //SET FURTHEST POINT AND TANGENTS !!!
        return new RawOBJModel(this.verticesArray, this.texturesArray, this.normalsArray, this.indicesArray, this.tangentsArray,  this.furthestPoint,
                this.jointIdsArray, this.weightsArray);
    }

    private void calculateTangents() {

    }

    private void readRawData() {
        this.readPositions();
        this.readNormals();
        this.readTextureCoords();
    }

    private void readPositions() {
        String positionsId = this.meshData.getChild("vertices").getChild("input").getAttribute("source").substring(1);
        XmlNode positionsData = this.meshData.getChildWithAttribute("source", "id", positionsId).getChild("float_array");
        int count = Integer.parseInt(positionsData.getAttribute("count"));
        String[] posData = positionsData.getData().split(" ");

        for(int i = 0; i < count / 3; ++i) {
            float x = Float.parseFloat(posData[i * 3]);
            float y = Float.parseFloat(posData[i * 3 + 1]);
            float z = Float.parseFloat(posData[i * 3 + 2]);
            Vector4f position = new Vector4f(x, y, z, 1.0F);
            Matrix4f.transform(this.correction, position, position);
            this.vertices.add(new VertexDAE(this.vertices.size(), new Vector3f(position.x, position.y, position.z), (VertexSkinData)this.vertexWeights.get(this.vertices.size())));
        }

    }

    private void readNormals() {
        String normalsId = this.meshData.getChild("polylist").getChildWithAttribute("input", "semantic", "NORMAL").getAttribute("source").substring(1);
        XmlNode normalsData = this.meshData.getChildWithAttribute("source", "id", normalsId).getChild("float_array");
        int count = Integer.parseInt(normalsData.getAttribute("count"));
        String[] normData = normalsData.getData().split(" ");

        for(int i = 0; i < count / 3; ++i) {
            float x = Float.parseFloat(normData[i * 3]);
            float y = Float.parseFloat(normData[i * 3 + 1]);
            float z = Float.parseFloat(normData[i * 3 + 2]);
            Vector4f norm = new Vector4f(x, y, z, 0.0F);
            Matrix4f.transform(this.correction, norm, norm);
            this.normals.add(new Vector3f(norm.x, norm.y, norm.z));
        }

    }

    private void readTextureCoords() {
        String texCoordsId = this.meshData.getChild("polylist").getChildWithAttribute("input", "semantic", "TEXCOORD").getAttribute("source").substring(1);
        XmlNode texCoordsData = this.meshData.getChildWithAttribute("source", "id", texCoordsId).getChild("float_array");
        int count = Integer.parseInt(texCoordsData.getAttribute("count"));
        String[] texData = texCoordsData.getData().split(" ");

        for(int i = 0; i < count / 2; ++i) {
            float s = Float.parseFloat(texData[i * 2]);
            float t = Float.parseFloat(texData[i * 2 + 1]);
            this.textures.add(new Vector2f(s, t));
        }

    }

    private void assembleVertices() {
        XmlNode poly = this.meshData.getChild("polylist");
        int typeCount = poly.getChildren("input").size();
        String[] indexData = poly.getChild("p").getData().split(" ");

        //NEW - tangents
        //String[] perPolyVertexes = poly.getChild("vcount").getData().split(" ");
        //int j = 0;
        //int polyVertexCount = Integer.parseInt(perPolyVertexes[j++]);
        int polyVertexCounter = 0;

        for(int i = 0; i < indexData.length / typeCount; ++i) {
            int positionIndex = Integer.parseInt(indexData[i * typeCount]);
            int normalIndex = Integer.parseInt(indexData[i * typeCount + 1]);
            int texCoordIndex = Integer.parseInt(indexData[i * typeCount + 2]);
            this.processVertex(positionIndex, normalIndex, texCoordIndex);

            if(++polyVertexCounter >= 3){
                //calculate tangents for past side
                calculateTangents(this.vertices.get(Integer.parseInt(indexData[(i-2) * typeCount])), this.vertices.get(Integer.parseInt(indexData[(i-1) * typeCount])), this.vertices.get(Integer.parseInt(indexData[i * typeCount])));

                polyVertexCounter = 0;
            }
        }

    }

    private VertexDAE processVertex(int posIndex, int normIndex, int texIndex) {
        VertexDAE currentVertex = (VertexDAE)this.vertices.get(posIndex);
        if (!currentVertex.isSet()) {
            currentVertex.setTextureIndex(texIndex);
            currentVertex.setNormalIndex(normIndex);
            this.indices.add(posIndex);
            return currentVertex;
        } else {
            return this.dealWithAlreadyProcessedVertex(currentVertex, texIndex, normIndex);
        }
    }

    //NEW
    private void calculateTangents(VertexDAE v0, VertexDAE v1, VertexDAE v2) {
        Vector3f delatPos1 = Vector3f.sub(v1.getPosition(), v0.getPosition(), null);
        Vector3f delatPos2 = Vector3f.sub(v2.getPosition(), v0.getPosition(), null);
        Vector2f uv0 = this.textures.get(v0.getTextureIndex());
        Vector2f uv1 = this.textures.get(v1.getTextureIndex());
        Vector2f uv2 = this.textures.get(v2.getTextureIndex());
        Vector2f deltaUv1 = Vector2f.sub(uv1, uv0, null);
        Vector2f deltaUv2 = Vector2f.sub(uv2, uv0, null);

        float r = 1.0f / (deltaUv1.x * deltaUv2.y - deltaUv1.y * deltaUv2.x);
        delatPos1.scale(deltaUv2.y);
        delatPos2.scale(deltaUv1.y);
        Vector3f tangent = Vector3f.sub(delatPos1, delatPos2, null);
        tangent.scale(r);
        v0.addTangent(tangent);
        v1.addTangent(tangent);
        v2.addTangent(tangent);
    }

    private int[] convertIndicesListToArray() {
        this.indicesArray = new int[this.indices.size()];

        for(int i = 0; i < this.indicesArray.length; ++i) {
            this.indicesArray[i] = (Integer)this.indices.get(i);
        }

        return this.indicesArray;
    }

    private float convertDataToArrays() {
        this.furthestPoint = 0.0F;

        for(int i = 0; i < this.vertices.size(); ++i) {
            VertexDAE currentVertex = (VertexDAE)this.vertices.get(i);
            if (currentVertex.getLength() > furthestPoint) {
                furthestPoint = currentVertex.getLength();
            }

            Vector3f position = currentVertex.getPosition();
            Vector2f textureCoord = (Vector2f)this.textures.get(currentVertex.getTextureIndex());
            Vector3f normalVector = (Vector3f)this.normals.get(currentVertex.getNormalIndex());
            Vector3f tangent = (Vector3f)currentVertex.getAverageTangent();
            this.verticesArray[i * 3] = position.x;
            this.verticesArray[i * 3 + 1] = position.y;
            this.verticesArray[i * 3 + 2] = position.z;
            this.texturesArray[i * 2] = textureCoord.x;
            this.texturesArray[i * 2 + 1] = 1.0F - textureCoord.y;
            this.normalsArray[i * 3] = normalVector.x;
            this.normalsArray[i * 3 + 1] = normalVector.y;
            this.normalsArray[i * 3 + 2] = normalVector.z;
            this.tangentsArray[i * 3] = tangent.x;
            this.tangentsArray[i * 3 + 1] = tangent.y;
            this.tangentsArray[i * 3 + 2] = tangent.z;
            VertexSkinData weights = currentVertex.getWeightsData();
            this.jointIdsArray[i * 3] = (Integer)weights.jointIds.get(0);
            this.jointIdsArray[i * 3 + 1] = (Integer)weights.jointIds.get(1);
            this.jointIdsArray[i * 3 + 2] = (Integer)weights.jointIds.get(2);
            this.weightsArray[i * 3] = (Float)weights.weights.get(0);
            this.weightsArray[i * 3 + 1] = (Float)weights.weights.get(1);
            this.weightsArray[i * 3 + 2] = (Float)weights.weights.get(2);
        }

        return furthestPoint;
    }

    private VertexDAE dealWithAlreadyProcessedVertex(VertexDAE previousVertex, int newTextureIndex, int newNormalIndex) {
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            this.indices.add(previousVertex.getIndex());
            return previousVertex;
        } else {
            VertexDAE anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null) {
                return this.dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex);
            } else {
                VertexDAE duplicateVertex = new VertexDAE(this.vertices.size(), previousVertex.getPosition(), previousVertex.getWeightsData());
                duplicateVertex.setTextureIndex(newTextureIndex);
                duplicateVertex.setNormalIndex(newNormalIndex);
                previousVertex.setDuplicateVertex(duplicateVertex);
                this.vertices.add(duplicateVertex);
                this.indices.add(duplicateVertex.getIndex());
                return duplicateVertex;
            }
        }
    }

    private void initArrays() {
        this.verticesArray = new float[this.vertices.size() * 3];
        this.texturesArray = new float[this.vertices.size() * 2];
        this.normalsArray = new float[this.vertices.size() * 3];
        this.tangentsArray = new float[this.vertices.size() * 3];
        this.jointIdsArray = new int[this.vertices.size() * 3];
        this.weightsArray = new float[this.vertices.size() * 3];
    }

    private void removeUnusedVertices() {
        Iterator var2 = this.vertices.iterator();

        while(var2.hasNext()) {
            VertexDAE vertex = (VertexDAE)var2.next();
            vertex.averageTangents();
            if (!vertex.isSet()) {
                vertex.setTextureIndex(0);
                vertex.setNormalIndex(0);
            }
        }

    }
}
