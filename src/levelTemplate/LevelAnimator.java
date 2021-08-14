package levelTemplate;

import Input.Config;
import renderEngine.GameMain;
import renderEngine.entities.Entity;
import renderEngine.entities.Light;
import renderEngine.gui.GuiTexture;
import renderEngine.text.fontMeshCreator.GUIText;
import renderEngine.toolbox.APSCounter;

import java.util.List;

public abstract class LevelAnimator <M extends LevelMain<?,?,?>> implements Runnable{

    public M levMain;

    //settings import
    public int targetAPS = Config.ANIMATION_THREAD_LOOP_TARGET_TIME;

    public LevelAnimator(M levMain) {
        this.levMain = levMain;
    }

    public abstract void passThisToMain();

    public void run() {
        passThisToMain();

        LevelThreadAction.doWait1(0, 0);

        spawnEntities();

        levMain.INIT_ANIMATOR_DONE = true;

        GameMain.setLastFrameTime();
        while (levMain.PLAY_LEVEL) {

            GameMain.setFrameRenderTime();
            GameMain.setLastFrameTime();
            // animate, key callback is being carried out separately in main thread
            animate();

            if(levMain.showFPS){
                APSCounter.addFrameAndRefreshCount();
            }

            //target Animation time set to 1ms per loop, if less then sleep
            long tmp = targetAPS-GameMain.getDeltaFrameTimeInMilis();
            if(tmp > 0) {
                try {
                    Thread.sleep(tmp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        LevelThreadAction.doNotify1(1);
    }

    protected abstract void spawnEntities();

    private float particleSortCounter = 0;
    private void animate() {
        animationLoop();

        if (levMain.loadingFlag) {
            levMain.loadingScreen.moveLoadingIcon();
        }
        //particle update
        LevelThreadAction.particleMutex.lock();
        levMain.particleRenderer.update(levMain.camera);
        LevelThreadAction.particleMutex.unlock();
        //particle sorting
        particleSortCounter += GameMain.getFrameRenderTime();
        if(particleSortCounter> Config.PARTICLES_SORT_INTERVAL) {
            LevelThreadAction.particleMutex.lock();
            levMain.particleRenderer.sortParticles();
            LevelThreadAction.particleMutex.unlock();
            particleSortCounter = 0;
        }
    }

    protected abstract void animationLoop();

    protected abstract void processDynamicText(List<GUIText> tList);

    protected abstract List<Entity> getShadowEntities();
    protected abstract List<Entity> getRenderEntities();
    protected abstract Light getSun();
    protected abstract List<Light> getLights();
    protected abstract List<GuiTexture> getGui();
}
