package renderEngine.loaders.obj;

import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.storage.RawOBJModel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OBJLoader {

    /**
     * FILE LOADER
     * for non-animated
     */

    public static RawOBJModel loadOBJFile(String objFileName) {
        FileReader isr = null;
        File objFile = new File(objFileName);
        try {
            isr = new FileReader(objFile);
        } catch (FileNotFoundException e) {
            System.err.println("File not found in res; don't use any extention");
        }
        assert isr != null;
        BufferedReader reader = new BufferedReader(isr);
        String line;
        List<VertexOBJ> vertices = new ArrayList<VertexOBJ>();
        List<Vector2f> textures = new ArrayList<Vector2f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Integer> indices = new ArrayList<Integer>();
        try {
            while (true) {
                line = reader.readLine();
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    VertexOBJ newVertexOBJ = new VertexOBJ(vertices.size(), vertex);
                    vertices.add(newVertexOBJ);

                } else if (line.startsWith("vt ")) {
                    String[] currentLine = line.split(" ");
                    Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]));
                    textures.add(texture);
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    normals.add(normal);
                } else if (line.startsWith("f ")) {
                    break;
                }
            }
            while (line != null && line.startsWith("f ")) {
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");
                VertexOBJ v0 = processVertex(vertex1, vertices, indices);
                VertexOBJ v1 = processVertex(vertex2, vertices, indices);
                VertexOBJ v2 = processVertex(vertex3, vertices, indices);
                calculateTangents(v0, v1, v2, textures);//NEW
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading the file");
        }
        removeUnusedVertices(vertices);
        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];
        float[] tangentsArray = new float[vertices.size() * 3];
        float furthest = convertDataToArrays(vertices, textures, normals, verticesArray,
                texturesArray, normalsArray, tangentsArray);
        int[] indicesArray = convertIndicesListToArray(indices);

        return new RawOBJModel(verticesArray, texturesArray, normalsArray, indicesArray, tangentsArray, furthest);
    }

    //NEW
    private static void calculateTangents(VertexOBJ v0, VertexOBJ v1, VertexOBJ v2,
                                          List<Vector2f> textures) {
        Vector3f delatPos1 = Vector3f.sub(v1.getPosition(), v0.getPosition(), null);
        Vector3f delatPos2 = Vector3f.sub(v2.getPosition(), v0.getPosition(), null);
        Vector2f uv0 = textures.get(v0.getTextureIndex());
        Vector2f uv1 = textures.get(v1.getTextureIndex());
        Vector2f uv2 = textures.get(v2.getTextureIndex());
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

    private static VertexOBJ processVertex(String[] vertex, List<VertexOBJ> vertices,
                                           List<Integer> indices) {
        int index = Integer.parseInt(vertex[0]) - 1;
        VertexOBJ currentVertexOBJ = vertices.get(index);
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        if (!currentVertexOBJ.isSet()) {
            currentVertexOBJ.setTextureIndex(textureIndex);
            currentVertexOBJ.setNormalIndex(normalIndex);
            indices.add(index);
            return currentVertexOBJ;
        } else {
            return dealWithAlreadyProcessedVertex(currentVertexOBJ, textureIndex, normalIndex, indices,
                    vertices);
        }
    }

    private static int[] convertIndicesListToArray(List<Integer> indices) {
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indicesArray.length; i++) {
            indicesArray[i] = indices.get(i);
        }
        return indicesArray;
    }

    private static float convertDataToArrays(List<VertexOBJ> vertices, List<Vector2f> textures,
                                             List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
                                             float[] normalsArray, float[] tangentsArray) {
        float furthestPoint = 0;
        for (int i = 0; i < vertices.size(); i++) {
            VertexOBJ currentVertexOBJ = vertices.get(i);
            if (currentVertexOBJ.getLength() > furthestPoint) {
                furthestPoint = currentVertexOBJ.getLength();
            }
            Vector3f position = currentVertexOBJ.getPosition();
            Vector2f textureCoord = textures.get(currentVertexOBJ.getTextureIndex());
            Vector3f normalVector = normals.get(currentVertexOBJ.getNormalIndex());
            Vector3f tangent = currentVertexOBJ.getAverageTangent();
            verticesArray[i * 3] = position.x;
            verticesArray[i * 3 + 1] = position.y;
            verticesArray[i * 3 + 2] = position.z;
            texturesArray[i * 2] = textureCoord.x;
            texturesArray[i * 2 + 1] = 1 - textureCoord.y;
            normalsArray[i * 3] = normalVector.x;
            normalsArray[i * 3 + 1] = normalVector.y;
            normalsArray[i * 3 + 2] = normalVector.z;
            tangentsArray[i * 3] = tangent.x;
            tangentsArray[i * 3 + 1] = tangent.y;
            tangentsArray[i * 3 + 2] = tangent.z;

        }
        return furthestPoint;
    }

    private static VertexOBJ dealWithAlreadyProcessedVertex(VertexOBJ previousVertexOBJ, int newTextureIndex,
                                                            int newNormalIndex, List<Integer> indices, List<VertexOBJ> vertices) {
        if (previousVertexOBJ.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            indices.add(previousVertexOBJ.getIndex());
            return previousVertexOBJ;
        } else {
            VertexOBJ anotherVertexOBJ = previousVertexOBJ.getDuplicateVertexOBJ();
            if (anotherVertexOBJ != null) {
                return dealWithAlreadyProcessedVertex(anotherVertexOBJ, newTextureIndex,
                        newNormalIndex, indices, vertices);
            } else {
                VertexOBJ duplicateVertexOBJ = previousVertexOBJ.duplicate(vertices.size());//NEW
                duplicateVertexOBJ.setTextureIndex(newTextureIndex);
                duplicateVertexOBJ.setNormalIndex(newNormalIndex);
                previousVertexOBJ.setDuplicateVertexOBJ(duplicateVertexOBJ);
                vertices.add(duplicateVertexOBJ);
                indices.add(duplicateVertexOBJ.getIndex());
                return duplicateVertexOBJ;
            }
        }
    }

    private static void removeUnusedVertices(List<VertexOBJ> vertices) {
        for (VertexOBJ vertexOBJ : vertices) {
            vertexOBJ.averageTangents();
            if (!vertexOBJ.isSet()) {
                vertexOBJ.setTextureIndex(0);
                vertexOBJ.setNormalIndex(0);
            }
        }
    }
}
