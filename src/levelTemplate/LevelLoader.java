package levelTemplate;

import renderEngine.loaders.collada.ColladaLoader;
import renderEngine.loaders.obj.HitBoxLoader;
import renderEngine.loaders.obj.OBJLoader;
import renderEngine.loaders.png.de.mathiasmann.twl.PNGHandler;
import renderEngine.physics.hitBox.HitBoxAABB_DYNAMIC;
import renderEngine.physics.hitBox.HitBoxAABB_FIXED;
import renderEngine.physics.hitBox.HitBoxHULL;
import renderEngine.physics.hitBox.HitBoxOBB;
import renderEngine.storage.RawOBJModel;
import renderEngine.storage.RawPNGTexture;
import renderEngine.storage.animation.Animation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class LevelLoader <M extends LevelMain<?,?,?>> implements Runnable {

    // -- VARIABLES SHARED WITH MAIN --
    //buffer for loaded file sets, models, gui, animations etc.
    ///USE IN LoadMutex!
//    public List<LoadedDataBuffer> preparedModels;
//    public List<LevelComponentInterface> levelComponentsList;
//    boolean INIT_LOADING_DONE;

    protected M levMain;

    public LevelLoader(M levMain) {
        this.levMain = levMain;
    }

    @Override
    public void run() {

        loadFiles();

        levMain.INIT_LOADING_DONE = true;
    }

    protected abstract void loadFiles();

    public void put(LoadedDataBuffer entrance) {
        LevelThreadAction.loadMutex.lock();
        levMain.preparedModels.add(entrance);
        LevelThreadAction.loadMutex.unlock();
    }
    public void put(List<LoadedDataBuffer> list) {
        LevelThreadAction.loadMutex.lock();
        levMain.preparedModels.addAll(list);
        LevelThreadAction.loadMutex.unlock();
    }
    public void put(LoadedDataBuffer[] array) {
        LevelThreadAction.loadMutex.lock();
        levMain.preparedModels.addAll(Arrays.asList(array));
        LevelThreadAction.loadMutex.unlock();
    }

    public static LoadedDataBuffer loadModelFiles(String objPath, String texturePath, String normalPath, String specularPath) {
        RawOBJModel objData = OBJLoader.loadOBJFile("res/"+objPath+".obj");
        RawPNGTexture texture = ( texturePath != null ) ? PNGHandler.decodeTextureFile("res/"+texturePath+".png") : null ;
        RawPNGTexture normal = ( normalPath != null ) ? PNGHandler.decodeTextureFile("res/"+normalPath+".png") : null ;
        RawPNGTexture specular = ( specularPath != null) ? PNGHandler.decodeTextureFile("res/"+specularPath+".png") : null ;
        return new LoadedDataBuffer(objData, texture, normal, specular);
    }

    public static LoadedDataBuffer loadAnimatedModelFiles(String colladaPath, String texturePath, String normalPath, String specularPath){
        RawOBJModel objData = ColladaLoader.loadColladaModel("res/"+colladaPath+".dae", 3);
        Animation animation = ColladaLoader.loadAnimation("res/"+colladaPath+".dae");
        RawPNGTexture texture = ( texturePath != null ) ? PNGHandler.decodeTextureFile("res/"+texturePath+".png") : null ;
        RawPNGTexture normal = ( normalPath != null ) ? PNGHandler.decodeTextureFile("res/"+normalPath+".png") : null ;
        RawPNGTexture specular = ( specularPath != null) ? PNGHandler.decodeTextureFile("res/"+specularPath+".png") : null ;
        return new LoadedDataBuffer(objData, animation, texture, normal, specular);
    }

    public static LoadedDataBuffer loadImageFile(String texturePath) {
        RawPNGTexture guiImage = PNGHandler.decodeTextureFile("res/"+texturePath+".png");
        return new LoadedDataBuffer(guiImage);
    }

    public static LoadedDataBuffer loadFontFiles(String texturePath, String infoPath) {
        RawPNGTexture fontImage = PNGHandler.decodeTextureFile("res/"+texturePath+".png");
        File file = new File(infoPath);
        return new LoadedDataBuffer(fontImage, file);
    }

    public static <H extends HitBoxHULL> HitBoxHULL loadRawHitBox(String objPath, Class<H> type){
        if(type == HitBoxAABB_DYNAMIC.class) {
            return HitBoxLoader.generateAABB_DYNAMICFromModel("res/" + objPath + ".obj");
        } else if(type == HitBoxAABB_FIXED.class){
            return HitBoxLoader.generateAABB_FIXEDFromModel("res/"+objPath+".obj");
        } else if (type == HitBoxOBB.class){
            return HitBoxLoader.loadOBJHitBoxOBB("res/"+objPath+".obj");
        } else {
            return HitBoxLoader.loadOBJHitBoxHULL("res/"+objPath+".obj");
        }
    }
    public static <H extends HitBoxHULL> LoadedDataBuffer loadHitBox(String objPath, Class<H> type){
        return new LoadedDataBuffer(loadRawHitBox(objPath, type));
    }
}
