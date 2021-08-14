package renderEngine.loaders.obj;

import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VertexOBJ {

    /**
     *  A class helping store temporary data inside OBJFileLoader while processing
     */

    private static final int NO_INDEX = -1;

    private Vector3f position;
    private int textureIndex = NO_INDEX;
    private int normalIndex = NO_INDEX;
    private VertexOBJ duplicateVertexOBJ = null;
    private int index;
    private float length;
    private List<Vector3f> tangents = new ArrayList<Vector3f>();
    private Vector3f averagedTangent = new Vector3f(0, 0, 0);

    protected VertexOBJ(int index, Vector3f position){
        this.index = index;
        this.position = position;
        this.length = position.length();
    }

    protected void addTangent(Vector3f tangent){
        tangents.add(tangent);
    }

    //NEW
    protected VertexOBJ duplicate(int newIndex){
        VertexOBJ vertexOBJ = new VertexOBJ(newIndex, position);
        vertexOBJ.tangents = this.tangents;
        return vertexOBJ;
    }

    protected void averageTangents(){
        if(tangents.isEmpty()){
            return;
        }
        for(Vector3f tangent : tangents){
            Vector3f.add(averagedTangent, tangent, averagedTangent);
        }
        averagedTangent.normalise();
    }

    protected Vector3f getAverageTangent(){
        return averagedTangent;
    }

    protected int getIndex(){
        return index;
    }

    protected float getLength(){
        return length;
    }

    protected boolean isSet(){
        return textureIndex!=NO_INDEX && normalIndex!=NO_INDEX;
    }

    protected boolean isNormalSet(){
        return normalIndex!=NO_INDEX;
    }

    protected boolean hasSameTextureAndNormal(int textureIndexOther,int normalIndexOther){
        return textureIndexOther==textureIndex && normalIndexOther==normalIndex;
    }

    protected boolean hasSameNormal(int normalIndexOther){
        return normalIndexOther==normalIndex;
    }

    protected void setTextureIndex(int textureIndex){
        this.textureIndex = textureIndex;
    }

    protected void setNormalIndex(int normalIndex){
        this.normalIndex = normalIndex;
    }

    protected Vector3f getPosition() {
        return position;
    }

    protected int getTextureIndex() {
        return textureIndex;
    }

    protected int getNormalIndex() {
        return normalIndex;
    }

    protected VertexOBJ getDuplicateVertexOBJ() {
        return duplicateVertexOBJ;
    }

    protected void setDuplicateVertexOBJ(VertexOBJ duplicateVertexOBJ) {
        this.duplicateVertexOBJ = duplicateVertexOBJ;
    }

}