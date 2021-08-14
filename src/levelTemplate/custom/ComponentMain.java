package levelTemplate.custom;

import levelComponents.HasText;
import levelTemplate.LevelMain;
import levelComponents.LevelComponent;
import levelTemplate.LoadedDataBuffer;
import levelTemplate.LoadingScreen;
import renderEngine.physics.hitBox.HitBoxHULL;
import renderEngine.text.fontMeshCreator.GUIText;

import java.util.ArrayList;
import java.util.List;

public abstract class ComponentMain extends LevelMain<ComponentMain, ComponentAnimator, ComponentLoader> {

    public List<HitBoxHULL> hitBoxes = new ArrayList<>();

    protected List<GUIText> staticTextList = new ArrayList<>();

    public List<LevelComponent> components = new ArrayList<>();

    public ComponentMain(){ super(ComponentMain.class); }

    public void play() {
        play(ComponentLoader.class, ComponentAnimator.class, ComponentLoadingScreen.class);
    }

    @Override
    protected void customLoad() {
        for(LevelComponent component : components){
            component.processToOpenGlLoop(loader, loadingScreen);
        }
    }

    @Override
    protected void passToOpenGL(LoadedDataBuffer pm) {
        switch (pm.type) {
            case HIT_BOX:
                hitBoxes.add(pm.hitBox);
        }
    }

    @Override
    protected boolean checkResetFbo() {
        return false;
    }

    @Override
    protected void resetCustomStuffWithFboReset() {
        //reset guis position and scale
        levAnimator.setGui();

        //reload and rescale static renderEngine.text.renderEngine.text
        //animator.removeStaticText();
        reloadFonts();
        //animator.loadStaticText();
    }

    private void reloadFonts() {
        for(LevelComponent component : components){
            if(component instanceof HasText){
                ((HasText) component).reloadFonts();
            }
        }
    }
}
