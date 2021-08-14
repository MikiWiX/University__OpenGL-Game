package levelTemplate;

import renderEngine.physics.hitBox.HitBoxHULL;
import renderEngine.storage.RawOBJModel;
import renderEngine.storage.RawPNGTexture;
import renderEngine.storage.animation.Animation;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class LoadedDataBuffer {

    public RawOBJModel objData;
    public RawPNGTexture textureData;
    public RawPNGTexture normalData;
    public RawPNGTexture specularData;
    public List<Animation> animationData;

    public float shineDamper = 1;
    public float reflectivity = 0;
    public float ambient = 0;

    //texture properties
    public float textureScale = 1;
    public float normalScale = 1;
    public float specularScale = 1;

    private boolean clampEdges = false;
    private boolean mipmap = false;
    private boolean anisotropic = false;
    private boolean nearest = false;

    public File fontInfoFile;
    public int fontPadding;

    public int numberOfTextureCols = 1;
    public int numberOfTextureRows = 1;

    public boolean alphaBlending = false;

    public HitBoxHULL hitBox;

    public enum Type {
        ANIMATED_MODEL,
        SHIP_MODEL,
        ALIEN_MODEL,
        STATIC_MODEL,
        GUI,
        FONT,
        PARTICLE,
        HIT_BOX
    }
    public Type type;

    /**
     * This class contains data TO BE LOAD
     * about raw image and vertex model data as well as
     * model properties such as reflectivity and
     * some extra info like if its going to be removed during game,
     * or where to bind it in renderEngine.entities lists
     * if model is animated, then about animation too
     *
     * @param objData raw Arrays containing OBJ file info
     * @param textureData ByteBuffer data containing raw PNG image
     * @param normalData ---same-as-above--- , but can be null
     * @param specularData ---same-as-above--- , but can be null
     */
    public LoadedDataBuffer(RawOBJModel objData, RawPNGTexture textureData, RawPNGTexture normalData, RawPNGTexture specularData){
        this.objData = objData;
        this.textureData = textureData;
        this.normalData = normalData;
        this.specularData = specularData;
    }

    /**
     * for animated models
     */
    public LoadedDataBuffer(RawOBJModel objData, Animation animationData, RawPNGTexture textureData, RawPNGTexture normalData, RawPNGTexture specularData){
        this.objData = objData;
        this.animationData = Collections.singletonList(animationData);
        this.textureData = textureData;
        this.normalData = normalData;
        this.specularData = specularData;
    }

    /**
     * for renderEngine.gui
     */
    public LoadedDataBuffer(RawPNGTexture guiImage){
        this.textureData = guiImage;
    }

    /**
     * for font files
     */
    public LoadedDataBuffer(RawPNGTexture fontImage, File fontInfoFile){
        this.textureData = fontImage;
        this.fontInfoFile = fontInfoFile;
    }
    /**
     * for HitBox
     */
    public <H extends HitBoxHULL> LoadedDataBuffer(H hitBox){
        this.hitBox = hitBox;
    }

    /**
     *  set texture properties
     */
    public void setTextureClampEdges() {
        clampEdges = true;
    }
    public void setTextureNormalMipMap() {
        mipmap = true;
        anisotropic = false;
        nearest = false;
    }
    public void setTextureNearestFiltering() {
        mipmap = false;
        anisotropic = false;
        nearest = true;
    }
    public void setTextureAnisotropic() {
        mipmap = true;
        anisotropic = true;
        nearest = false;
    }

    public boolean getTextureMipmap() {
        return mipmap;
    }
    public boolean getTextureAnisotropic() {
        return anisotropic;
    }
    public boolean getTextureNearest() {
        return nearest;
    }
    public boolean getTextureClampEdges() {
        return clampEdges;
    }

    /**
     * add more animations
     */
    public void addAnimation(Animation animation){
        if(animationData==null){
            animationData = Collections.singletonList(animation);
        } else {
            animationData.add(animation);
        }
    }
}
