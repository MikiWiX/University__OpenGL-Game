package levelTemplate.custom;

import levelComponents.*;
import levelTemplate.LevelAnimator;
import levelTemplate.LevelThreadAction;
import renderEngine.GameMain;
import renderEngine.entities.Entity;
import renderEngine.entities.Light;
import renderEngine.gui.GuiTexture;
import renderEngine.particles.ParticleSystem;
import renderEngine.text.fontMeshCreator.GUIText;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.List;
import java.util.stream.Collectors;

public class ComponentAnimator extends LevelAnimator<ComponentMain> {

    public <M extends ComponentMain> ComponentAnimator(M levMain) {
        super(levMain);
    }

    @Override
    public void passThisToMain() {
        levMain.setLevAnimator(this);
    }

    @Override
    protected void spawnEntities() {
        for(LevelComponent component : levMain.components){
            component.init();
            if(component instanceof HasParticles){
                ((HasParticles) component).spawnParticleSystems(levMain.particleRenderer);
            }
        }
    }

    @Override
    protected void animationLoop() {
        levMain.camera.move();
        for(LevelComponent component : levMain.components){
            component.update();
        }
    }

    protected void setGui() {
        for(LevelComponent component : levMain.components){
            if(component instanceof HasGui){
                ((HasGui) component).setGui();
            }
        }
    }

    @Override
    protected void processDynamicText(List<GUIText> tList) {
        for(LevelComponent component : levMain.components){
            if(component instanceof HasText){
                ((HasText) component).processDynamicTexts(tList);
            }
        }
    }

    @Override
    protected List<Entity> getShadowEntities() {
        return getRenderEntities();
    }

    @Override
    protected List<Entity> getRenderEntities() {
        List<Entity> el = levMain.components.stream()
                .filter(c -> c instanceof HasEntities)
                .flatMap(c -> ((HasEntities) c).getEntityStream())
                .collect(Collectors.toList());
        return el;
    }

    @Override
    protected Light getSun() {
        return levMain.components.stream()
                .filter(c -> c instanceof HasLights)
                .map(c -> ((HasLights) c).getSun())
                .findFirst()
                .orElse( new Light(new Vector3f(1), new Vector3f(0)) );
    }

    @Override
    protected List<Light> getLights() {
        return levMain.components.stream()
                .filter(c -> c instanceof HasLights)
                .flatMap(c -> ((HasLights) c).getLightStream())
                .collect(Collectors.toList());
    }

    @Override
    protected List<GuiTexture> getGui() {
        return levMain.components.stream()
                .filter(c -> c instanceof HasGui)
                .flatMap(c -> ((HasGui) c).getGuiStream())
                .collect(Collectors.toList());
    }

    public static void generateParticles (ParticleSystem particleGenerator, Vector3f position){
        LevelThreadAction.particleMutex.lock();
        particleGenerator.generateParticles(position, GameMain.getFrameRenderTime());
        LevelThreadAction.particleMutex.unlock();
    }
    public static void generateParticles (List<ParticleSystem> particleGenerators, List<Vector3f> positions){
        LevelThreadAction.particleMutex.lock();
        for(int i=0; i<particleGenerators.size(); i++){
            particleGenerators.get(i).generateParticles(positions.get(i), GameMain.getFrameRenderTime());
        }

        LevelThreadAction.particleMutex.unlock();
    }

}
