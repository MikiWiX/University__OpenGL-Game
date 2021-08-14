package renderEngine.entities;

import renderEngine.GameMain;
import renderEngine.entities.entityComponents.CmpInterface;
import renderEngine.storage.Model;
import renderEngine.toolbox.Maths;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Entity {

    private Model model;
    private List<Model> models = new ArrayList<>();
    private int currentModelIndex;
    private Vector3f position;
    private float rotX, rotY, rotZ;
    private Vector3f scale;
    private Matrix4f transformationMatrix;

    private float shineDamper;
    private float reflectivity;
    private float ambient;

    private int textureIndex = 0;

    public Entity(Model model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale) {
        this.model = model;
        this.models.add(model);
        this.position = position;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
        updateTransformationMatrix();
        this.shineDamper = model.getShineDamper();
        this.reflectivity = model.getReflectivity();
        this.ambient = model.getAmbient();
        this.currentModelIndex = 0;
    }

    private List<CmpInterface> componentList = new ArrayList<>();

    public <T extends CmpInterface> void addComponent (T cmp) {
        for(CmpInterface cmpi : componentList){
            if(cmpi.getClass().equals(cmp.getClass())){
                return;
            }
        }
        cmp.setEntity(this);
        cmp.init();
        componentList.add(cmp);
    }

    @SuppressWarnings("unchecked")
    public <T extends CmpInterface> T getComponent(Class<T> className) {
        for(CmpInterface cin : componentList) {
            if(cin.getClass().equals(className)){
                return (T) cin;
            }
        }
        return null;
    }

    public void update() {
        //update components
        for(CmpInterface cin : componentList) {
            cin.update();
        }
        updateTransformationMatrix();
        customUpdate();
    }

    public void customUpdate(){};

    public void updateTransformationMatrix() {
        transformationMatrix = Maths.createTransformationMatrixFromEntity(this);
    }
    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }

    public float getTextureXOffset() {
        int column = (int) (textureIndex % model.getNumberOfRows());
        return (float) column/ (float) model.getNumberOfRows();
    }
    public float getTextureYOffset() {
        int row = (int) (textureIndex/model.getNumberOfRows());
        return (float) row/ (float) model.getNumberOfRows();
    }

    //methods for moving and rotating object relatively to current position/rotation
    public void increasePosition(float dx, float dy, float dz) {
        float ft = GameMain.getFrameRenderTime();
        this.position.x+=dx*ft;
        this.position.y+=dy*ft;
        this.position.z+=dz*ft;
    }
    public void fixedIncreasePosition(float dx, float dy, float dz) {
        this.position.x+=dx;
        this.position.y+=dy;
        this.position.z+=dz;
    }

    public void increaseRotation(float dx, float dy, float dz) {
        float ft = GameMain.getFrameRenderTime();
        this.rotX+=dx*ft;
        this.rotY+=dy*ft;
        this.rotX+=dz*ft;
    }

    //GETTERS AND SETTERS

    public Model getModel() {
        return model;
    }

    public void pickModel(int id){
        this.model = this.models.get(id);
        this.currentModelIndex = id;
    }

    public List<Model> getModels() {
        return models;
    }

    public void addModel(Model model){
        this.models.add(model);
    }

    public Vector3f getPosition() {
        return position;
    }
    public Vector3f copyPosition() {
        return new Vector3f(position.x, position.y, position.z);
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
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

    public void setTextureIndex(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public int getCurrentModelIndex() {
        return currentModelIndex;
    }
}
