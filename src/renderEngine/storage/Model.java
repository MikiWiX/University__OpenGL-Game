package renderEngine.storage;

import renderEngine.physics.hitBox.HitBoxHULL;
import renderEngine.storage.animation.Joint;
import renderEngine.storage.animation.Animation;
import renderEngine.storage.animation.Animator;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

// complete representation of old TexturedModel class
public class Model {

    // RawModel Data
    // contains IndexVBO count
    private Vao vao;

    public Model(Vao vao) {
        this.vao = vao;
    }

    private int texture;
    private int normalMap;
    private int specularMap;

    private HitBoxHULL hitBox;

    private boolean hasTexture = false;
    private boolean hasNormalMap = false;
    private boolean hasSpecularMap = false;
    private boolean hasAnimation = false;
    //if it's animated
    private Joint rootJoint;
    private int jointCount;
    private List<Animation> animationData;

    private float shineDamper = 1;
    private float reflectivity = 0;
    private float ambient = 0;

    private float textureScale = 1;
    private float normalScale = 1;
    private float specularScale = 1;

    //if the texture is a texture atlas
    private int numberOfRows = 1;

    public Vao getVao() {
        return this.vao;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public float getShineDamper() {
        return shineDamper;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

    public float getAmbient() {
        return ambient;
    }

    public void setAmbient(float ambient) {
        this.ambient = ambient;
    }

    public float getTextureScale() {
        return textureScale;
    }

    public void setTextureScale(float texture_scale) {
        this.textureScale = texture_scale;
    }

    public int getTexture() {
        return texture;
    }

    public void setTexture(int texture) {
        this.texture = texture;
        this.hasTexture = true;
    }

    public boolean hasTexture() {
        return this.hasTexture;
    }

    public float getNormalScale() {
        return normalScale;
    }

    public void setNormalScale(float normalScale) {
        this.normalScale = normalScale;
    }

    public int getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(int normalMap) {
        this.normalMap = normalMap;
        this.hasNormalMap = true;
    }

    public boolean hasNormalMap() {
        return this.hasNormalMap;
    }

    public float getSpecularScale() {
        return specularScale;
    }

    public void setSpecularScale(float specularScale) {
        this.specularScale = specularScale;
    }

    public int getSpecularMap() {
        return specularMap;
    }

    public void setSpecularMap(int specularMap) {
        this.specularMap = specularMap;
        this.hasSpecularMap = true;
    }

    public boolean hasSpecularMap() {
        return this.hasSpecularMap;
    }

    public void delete() {
        vao.delete();
        glDeleteTextures(texture);
        glDeleteTextures(normalMap);
        glDeleteTextures(specularMap);
    }

    public void makeAnimated(Joint joint, int jointCount) {
        this.hasAnimation = true;
        this.rootJoint = joint;
        this.jointCount = jointCount;
        //this.animator = new Animator(this);
        rootJoint.calcInverseBindTransform(new Matrix4f());
    }
    public void saveAnimation(Animation animation){
        if(animationData == null){
            animationData = new ArrayList<>();
        }
        animationData.add(animation);
    }
    public void saveAnimation(List<Animation> animation){
        if(animationData == null){
            animationData = new ArrayList<>();
        }
        animationData.addAll(animation);
    }

    public Animation getAnimation(int index){
        return (index<animationData.size())? animationData.get(index) : (animationData.isEmpty())? null : animationData.get(animationData.size()-1);
    }

    public boolean hasAnimation(){
        return hasAnimation;
    }

    public Joint getRootJoint() {
        return this.rootJoint;
    }

    public int getJointCount(){
        return jointCount;
    }

    public HitBoxHULL getHitBox() {
        return hitBox;
    }

    public <H extends HitBoxHULL> void setHitBox(H hitBox) {
        this.hitBox = hitBox;
    }
}
