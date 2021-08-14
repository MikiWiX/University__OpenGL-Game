package renderEngine.particles;

import renderEngine.entities.cameras.Camera;
import renderEngine.storage.Loader;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.GameMain;
import renderEngine.toolbox.ParticleInsertionSort;

import java.util.*;

public class ParticleMaster {

    private Map<ParticleTexture, List<Particle>> particles = new HashMap<>();
    private ParticleRenderer renderer;

    public ParticleMaster(Loader loader, Matrix4f projectionMatrix){
        renderer = new ParticleRenderer(loader, projectionMatrix);
    }

    public void update(Camera camera) {
        Iterator<Map.Entry<ParticleTexture, List<Particle>>> mapIterator = particles.entrySet().iterator();
        while(mapIterator.hasNext()) {
            List<Particle> list = mapIterator.next().getValue();
            Iterator<Particle> iterator = list.iterator();
            while(iterator.hasNext()){
                Particle p = iterator.next();
                boolean stillAlive = p.update(camera, GameMain.getFrameRenderTime());
                if(!stillAlive) {
                    iterator.remove();
                    if(list.isEmpty()){
                        mapIterator.remove();
                    }
                }
            }
        }
    }

    public void renderParticles(Camera camera){
        renderer.render(particles, camera);
    }

    public void sortParticles(){
        Iterator<Map.Entry<ParticleTexture, List<Particle>>> mapIterator = particles.entrySet().iterator();
        while(mapIterator.hasNext()) {
            ParticleTexture tex = mapIterator.next().getKey();
            if(!tex.getAlphaBlending()) {
                List<Particle> list = particles.get(tex);
                ParticleInsertionSort.sortHighToLow(list);
            }
        }
    }

    public void cleanUP() {
        renderer.cleanUP();
    }

    public void resetParticleRenderer(Matrix4f projectionMatrix) {
        renderer.reloadProjectionMatrix(projectionMatrix);
    }

    public void addParticle(Particle particle){
        List<Particle> list = particles.get(particle.getTexture());
        if(list==null) {
            list = new ArrayList<>();
            particles.put(particle.getTexture(), list);
        }
        list.add(particle);
    }
}
