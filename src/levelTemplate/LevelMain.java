package levelTemplate;

import Input.Settings;
import renderEngine.FBO.PostProcessing;
import renderEngine.FBO.ScreenFBO;
import renderEngine.GameMain;
import renderEngine.RenderLoopGLThread;
import renderEngine.entities.Entity;
import renderEngine.entities.cameras.Camera;
import renderEngine.entities.cameras.SimpleCam;
import renderEngine.gui.GuiRenderer;
import renderEngine.loaders.collada.JointsData;
import renderEngine.particles.ParticleMaster;
import renderEngine.renderer.EntityRenderer;
import renderEngine.shadows.ShadowMapRenderer;
import renderEngine.storage.Loader;
import renderEngine.storage.Model;
import renderEngine.storage.animation.Joint;
import renderEngine.text.fontMeshCreator.GUIText;
import renderEngine.text.fontRendering.TextMaster;
import renderEngine.toolbox.APSCounter;
import renderEngine.toolbox.FPSCounter;
import renderEngine.toolbox.MousePicker;
import renderEngine.toolbox.MyFile;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;

public abstract class LevelMain <M extends LevelMain<M,A,L>,  A extends LevelAnimator<M>,
        L extends LevelLoader<M>> {

    public final long window;
    protected Loader loader;
    public Camera camera;
    public LoadingScreen loadingScreen;
    public A levAnimator;
    protected Class<M> thisClass;

    public GuiRenderer guiRenderer;
    public TextMaster textRenderer;
    public ParticleMaster particleRenderer;
    protected PostProcessing postProcessing;
    protected ScreenFBO multiSampledFbo, resolvedFbo, regularFbo;
    protected ShadowMapRenderer shadowRenderer;
    protected EntityRenderer entityRenderer;
    public MousePicker mousePicker;

    //tools
    public Random rand = new Random();
    protected static final MyFile RES_FOLDER = new MyFile("res");
    protected boolean showFPS = Settings.SHOW_FPS;

    //buffer for loaded file sets, models, gui, animations etc.
    ///USE IN LoadMutex!
    public List<LoadedDataBuffer> preparedModels = new ArrayList<>();

    // FLAGS
    protected boolean PLAY_LEVEL = true;
    protected boolean INIT_LOADING_DONE = false;
    protected boolean INIT_ANIMATOR_DONE = false;
    private boolean currentlyMultiSampling = false;
    public boolean loadingFlag = false;

    //settingsImport
    protected boolean antiAlias = Settings.ANTI_ALIAS_ON;

    public void setLevAnimator(A levAnim) {
        this.levAnimator = levAnim;
    }

    public LevelMain(Class<M> thisClass) {
        this.window = GameMain.getWindow();
        this.loader = new Loader();
        this.thisClass = thisClass;
    }

    public <S extends LoadingScreen> void
    play(Class<L> loaderClass, Class<A> animatorClass, Class<S> loadingScreenClass) {

        //init, camera, loader and animator threads
        try {
            this.camera = SimpleCam.class.getConstructor(Vector3f.class, float.class, float.class, float.class)
                    .newInstance(new Vector3f(0, 100, 360), 0f, 0f, 0f);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        init();

        LevelThreadAction.createSignals(2);

        addComponents();

        try {
            this.loadingScreen = loadingScreenClass.getConstructor(LevelMain.class).newInstance(this);
            Thread levLoader = new Thread(loaderClass.getConstructor(thisClass).newInstance(this));
            levLoader.start();
            Thread levAnimator = new Thread(animatorClass.getConstructor(thisClass).newInstance(this));
            levAnimator.start();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

        loadLoop();

        while (PLAY_LEVEL && !glfwWindowShouldClose(window)) {
            renderLoop();
        }
        cleanUP();
    }

    public abstract void addComponents();

    public void exitLevel() {
        PLAY_LEVEL = false;
    }

    private void init() {
        entityRenderer = new EntityRenderer(camera);
        shadowRenderer = new ShadowMapRenderer(camera);
        entityRenderer.bindShadows(shadowRenderer);

        mousePicker = new MousePicker(camera, camera.getProjectionMatrix());
        guiRenderer = new GuiRenderer(loader);
        textRenderer = new TextMaster(loader);
        particleRenderer = new ParticleMaster(loader, camera.getProjectionMatrix());
        postProcessing = new PostProcessing(loader);

        if (antiAlias) {
            multiSampledFbo = new ScreenFBO(GameMain.getInWidth(), GameMain.getInHeight());
            resolvedFbo = new ScreenFBO(GameMain.getInWidth(), GameMain.getInHeight(), ScreenFBO.DEPTH_TEXTURE);
            currentlyMultiSampling = true;
        } else {
            regularFbo = new ScreenFBO(GameMain.getInWidth(), GameMain.getInHeight(), ScreenFBO.DEPTH_RENDER_BUFFER);
            currentlyMultiSampling = false;
        }
    }

    /**
     * loading function
     */
    private void loadLoop() {
        //first render
        loadingScreen.loadLoadingGui();
        loadingScreen.resetLoadingScreen();

        customLoad();

        while(!INIT_LOADING_DONE){
            //check for loaded objects to bind
            LevelThreadAction.loadMutex.lock();
            if(!preparedModels.isEmpty()){
                for (LoadedDataBuffer pm : preparedModels) {
                    passToOpenGL(pm);
                }
                preparedModels.clear();
            }
            LevelThreadAction.loadMutex.unlock();

            //render screen
            loadingScreen.renderLoadingScreen();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LevelThreadAction.loadMutex.lock();
        if(!preparedModels.isEmpty()){
            for (LoadedDataBuffer pm : preparedModels) {
                passToOpenGL(pm);
            }
            preparedModels.clear();
        }
        LevelThreadAction.loadMutex.unlock();

        LevelThreadAction.doNotify1(0);

        //animator.loadStaticText();
        while(!INIT_ANIMATOR_DONE) {
            loadingScreen.renderLoadingScreen();
        }
    }

    protected abstract void customLoad();

    /**
     * process loaded data (load to OpenGl and so on...)
     */
    protected abstract void passToOpenGL(LoadedDataBuffer pm);

    public static int loadAsImage(Loader loader, LoadedDataBuffer pm) {
        return loader.loadGuiImage(pm.textureData);
    }

    public static Model loadAsObject(Loader loader, LoadedDataBuffer pm) {

        Model model = loader.createModelFromOBJFile(pm.objData);

        boolean[] texDetail = {pm.getTextureMipmap(), pm.getTextureAnisotropic(), pm.getTextureNearest(), pm.getTextureClampEdges()};
        if (pm.textureData != null) {
            int texID = loader.loadTextureToOpenGL(pm.textureData, texDetail);
            model.setTexture(texID);
        }
        if (pm.normalData != null) {
            int normID = loader.loadTextureToOpenGL(pm.normalData, texDetail);
            model.setNormalMap(normID);
        }
        if (pm.specularData != null) {
            int specID = loader.loadTextureToOpenGL(pm.specularData, texDetail);
            model.setSpecularMap(specID);
        }

        model.setShineDamper(pm.shineDamper);
        model.setReflectivity(pm.reflectivity);
        model.setAmbient(pm.ambient);

        model.setTextureScale(pm.textureScale);
        model.setNormalScale(pm.normalScale);
        model.setSpecularScale(pm.specularScale);

        if(pm.objData.getIsAnimated()) {
            JointsData skeletonData = pm.objData.getJointsData();
            Joint headJoint = Joint.createJointTreeFromJointsData(skeletonData.headJoint);
            model.makeAnimated(headJoint, skeletonData.jointCount);
            model.saveAnimation(pm.animationData);
        }

        if(pm.hitBox!=null) {
            model.setHitBox(pm.hitBox);
        }

        return model;
    }

    /**
     * prepare to render
     */
    public void renderLoop() {
        if (GameMain.getResetFbo()) {
            //if any changes to view, reload but do not render (still animate)
            if(!GameMain.getNotYetReset()) {
                resetFbo();
            }
            GameMain.falseNotYetReset();
            glClearColor(0,0,0,0);
            glClear(GL_COLOR_BUFFER_BIT);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (checkResetFbo()) {
            resetFbo();
        } else {
            render();
        }

        glfwSwapBuffers(window); // swap the color buffers
    }

    /**
     * @return true if fbo got to reset, if false, render scene
     */
    protected abstract boolean checkResetFbo();

    /**
     * reset screen class, runs after resizing or settings change
     */
    private void resetFbo() {
        //don't reset renderEngine.FBO next time
        GameMain.zeroResetFbo();

        Matrix4f projectionMatrix = camera.createProjectionMatrix();

        entityRenderer.resetProjectionMatrix(projectionMatrix);

        if (currentlyMultiSampling) {
            multiSampledFbo.cleanUP();
            resolvedFbo.cleanUP();
        } else {
            regularFbo.cleanUP();
        }

        if (Settings.ANTI_ALIAS_ON) {
            multiSampledFbo = new ScreenFBO(GameMain.getInWidth(), GameMain.getInHeight());
            resolvedFbo = new ScreenFBO(GameMain.getInWidth(), GameMain.getInHeight(), ScreenFBO.DEPTH_TEXTURE);
            currentlyMultiSampling = true;
        } else {
            regularFbo = new ScreenFBO(GameMain.getInWidth(), GameMain.getInHeight(), ScreenFBO.DEPTH_RENDER_BUFFER);
            currentlyMultiSampling = false;
        }

        mousePicker.reset(projectionMatrix);

        particleRenderer.resetParticleRenderer(projectionMatrix);
        loadingScreen.resizeLoadingGui();

        RenderLoopGLThread.reloadFPSFont();

        resetCustomStuffWithFboReset();
    }

    protected abstract void resetCustomStuffWithFboReset();

    /**
     * actual render
     */
    private void render() {

        preRender();

        if (currentlyMultiSampling) {
            multiSampledFbo.bindFrameBuffer();
            renderScene();
            multiSampledFbo.unbindFrameBuffer();
            multiSampledFbo.resolveFbo(resolvedFbo);
            postProcessing.doPostProcessing(resolvedFbo.getColourTexture());
        } else {
            regularFbo.bindFrameBuffer();
            renderScene();
            regularFbo.unbindFrameBuffer();
            postProcessing.doPostProcessing(regularFbo.getColourTexture());
        }

        postRender();

        if(showFPS){
            FPSCounter.addFrameAndRefreshCount();
            List<GUIText> tList = new ArrayList<>();
            addFPS(tList);
            textRenderer.render();
            removeDynamicTexts(tList);
        }
    }

    private int shadowTexture;

    protected void preRender() {
        shadowTexture = shadowRenderer.renderShadowMap(levAnimator.getShadowEntities(), levAnimator.getSun());
    }

    protected void renderScene() {
        clearColor();
        for (Entity i : levAnimator.getRenderEntities()) {
            entityRenderer.processEntity(i);
        }
        entityRenderer.render(levAnimator.getLights(), camera, shadowTexture); //renders all renderEngine.entities with lights and camera

        //System.out.println(glGetError());

        LevelThreadAction.particleMutex.lock();
        particleRenderer.renderParticles(camera);
        LevelThreadAction.particleMutex.unlock();
    }

    protected void postRender() {
        // --- RENDER SHADOW MAP VIEW AS GUI ---
        /*List<GuiTexture> guis2 = new ArrayList<GuiTexture>();
        GuiTexture guix = new GuiTexture(shadowTexture, new Vector2f(0.5f,0.5f), new Vector2f(0.25f,0.25f), 0);
        guis2.add(guix);
        guiRenderer.render(guis2);*/

        guiRenderer.render(levAnimator.getGui()); //render renderEngine.gui

        List<GUIText> tList = new ArrayList<>();
        levAnimator.processDynamicText(tList);
        textRenderer.render();
        removeDynamicTexts(tList);

        if (loadingFlag) {
            loadingScreen.renderLoadingIcon();
        }
    }

    private static float clearColorRed = 1, clearColorGreen = 1, clearColorBlue =1 ;

    protected static void clearColor() {
        glClearColor(clearColorRed, clearColorGreen, clearColorBlue, 0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    protected void addFPS(List<GUIText> tList) {
        GUIText fps = new GUIText(String.valueOf(FPSCounter.getFPSCount()), textRenderer, 1.5f, RenderLoopGLThread.FPS_FONT, new Vector2f(0,0), 10,false);
        fps.setCharacterColor(1,1,1);
        fps.setBorderColor(0,0,0);
        fps.setBorderWidth(0.6f);
        tList.add(fps);

        GUIText aps = new GUIText(String.valueOf(APSCounter.getAPSCount()), textRenderer, 1f, RenderLoopGLThread.FPS_FONT, new Vector2f(0,0.05f), 10,false);
        aps.setCharacterColor(1,1,0.5f);
        aps.setBorderColor(0,0,0);
        aps.setBorderWidth(0.6f);
        tList.add(aps);
    }

    protected void removeDynamicTexts(List<GUIText> tList) {
        for (GUIText t : tList) {
            t.remove();
            t.cleanUP();
        }
        tList = null;
    }

    public static void setClearColor(float red, float green, float blue){
        clearColorRed = red;
        clearColorGreen = green;
        clearColorBlue = blue;
    }

    /**
     * cleanUP
     */
    public void cleanUP() {
        PLAY_LEVEL = false;
        LevelThreadAction.doWait1(1, 0);

        particleRenderer.cleanUP();
        textRenderer.cleanUP();
        postProcessing.cleanUP();
        guiRenderer.cleanUP();
        if (currentlyMultiSampling) {
            multiSampledFbo.cleanUP();
            resolvedFbo.cleanUP();
        } else {
            regularFbo.cleanUP();
        }
        entityRenderer.cleanUP();
        loader.cleanUP();
        LevelThreadAction.cleanUP();
    }
}
