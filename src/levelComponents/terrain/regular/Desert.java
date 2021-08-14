package levelComponents.terrain.regular;

import levelComponents.HasEntities;
import levelComponents.HasLights;
import levelComponents.HasParticles;
import levelComponents.LevelComponent;
import levelComponents.aliens.TestAliens;
import levelTemplate.LevelLoader;
import levelTemplate.LevelMain;
import levelTemplate.custom.ComponentAnimator;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LoadedDataBuffer;
import renderEngine.entities.Entity;
import renderEngine.entities.Light;
import renderEngine.entities.entityComponents.CmpAnimation;
import renderEngine.particles.ParticleMaster;
import renderEngine.particles.ParticleSystem;
import renderEngine.particles.ParticleTexture;
import renderEngine.storage.Loader;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static levelTemplate.LoadedDataBuffer.Type.PARTICLE;
import static levelTemplate.LoadedDataBuffer.Type.STATIC_MODEL;

public class Desert extends LevelComponent implements HasEntities, HasLights, HasParticles {

    public List<Model> terrainModels = new ArrayList<>();
    public List<Entity> terrain = new ArrayList<>();

    public ParticleSystem particleGenerator0;
    public ParticleSystem particleGenerator1;
    private List<ParticleTexture> particleTexture = new ArrayList<>();

    protected Light sun = new Light(new Vector3f(-10000, 12000, 10000), new Vector3f(1.3f, 1.2f, 1.2f));
    protected List<Light> lights = new ArrayList<>();

    public Desert(ComponentMain levMain, List<LevelComponent> cmpList){
        super(levMain, cmpList);
    }

    @Override
    protected void loadFiles() {
        LoadedDataBuffer stone = LevelLoader.loadModelFiles("stone/stoneO1", "stone/stoneT1_Loop", "stone/stoneN1_4K", "stone/stoneS1_4K");
        stone.shineDamper = 10f;
        stone.reflectivity = 0.8f;
        stone.ambient = 0.2f;
        stone.textureScale = 8f;
        stone.normalScale = 4f;
        stone.specularScale = 4f;
        stone.setTextureAnisotropic();
        stone.type = STATIC_MODEL;
        put(stone);

        LoadedDataBuffer sand = LevelLoader.loadModelFiles("sand/sandO1", "sand/sandT2", "sand/noise", null);
        sand.shineDamper = 10f;
        sand.reflectivity = 0.5f;
        sand.ambient = 0.25f;
        sand.textureScale = 16f;
        sand.normalScale = 120f;
        sand.setTextureAnisotropic();
        sand.type = STATIC_MODEL;
        put(sand);

        sand = LevelLoader.loadModelFiles("sand/outerSand", "sand/sandT2", "sand/noise", null);
        sand.shineDamper = 10f;
        sand.reflectivity = 0.5f;
        sand.ambient = 0.25f;
        sand.textureScale = 20f;
        sand.normalScale = 150f;
        sand.setTextureAnisotropic();
        sand.type = STATIC_MODEL;
        put(sand);

        LoadedDataBuffer model1 = LevelLoader.loadAnimatedModelFiles("model", "diffuse", null, null);
        model1.ambient = 0.2f;
        model1.setTextureAnisotropic();
        model1.type = STATIC_MODEL;
        put(model1);

        LoadedDataBuffer tex0 = LevelLoader.loadImageFile("dust/DustColorful");
        //tex0.alphaBlending = true;
        tex0.type = PARTICLE;
        put(tex0);

        tex0 = LevelLoader.loadImageFile("dust/DustWide");
        //tex0.alphaBlending = true;
        tex0.type = PARTICLE;
        put(tex0);
    }

    @Override
    protected void loadToOpenGL(Loader loader, LoadedDataBuffer pm) {
        switch (pm.type){
            case STATIC_MODEL:
                terrainModels.add(LevelMain.loadAsObject(loader, pm));
                break;
            case PARTICLE:
                particleTexture.add( new ParticleTexture(LevelMain.loadAsImage(loader, pm), pm.numberOfTextureCols, pm.numberOfTextureRows, pm.alphaBlending) );
        }
    }

    @Override
    public void init() {
        Entity t0 = new Entity(terrainModels.get(0), new Vector3f(40, -8, -260), 0, 180, 2, new Vector3f(400));
        Entity t1 = new Entity(terrainModels.get(1), new Vector3f(40, -8, -260), 0, 180, 2, new Vector3f(400));
        Entity t2 = new Entity(terrainModels.get(2), new Vector3f(40, -8, -260), 0, 180, 2, new Vector3f(400));
        terrain.add(t0);
        terrain.add(t1);
        terrain.add(t2);

        //animatedModel test
        Entity am0 = new Entity(terrainModels.get(3), new Vector3f(0, 10, 500), 0, 180, 0, new Vector3f(12));
        am0.addComponent(new CmpAnimation());
        am0.getComponent(CmpAnimation.class).doAnimation(terrainModels.get(3).getAnimation(0));
        terrain.add(am0);
    }

    @Override
    public void spawnParticleSystems(ParticleMaster renderer) {
        particleGenerator0 = new ParticleSystem(renderer, particleTexture.get(1), 10, 160, 0, 7, 150);
        particleGenerator0.randomizeRotation(90);
        particleGenerator0.addRotation(90);
        particleGenerator0.setDirection(new Vector3f(1f,0f,0f), 0.08f);
        particleGenerator0.setLifeError(0);
        particleGenerator0.setSpeedError(0.7f);
        particleGenerator0.setScaleError(0.5f);

        particleGenerator1 = new ParticleSystem(renderer, particleTexture.get(0), 10, 160, 0, 7, 150);
        particleGenerator1.randomizeRotation(90);
        particleGenerator1.addRotation(90);
        particleGenerator1.setDirection(new Vector3f(1f,0f,0f), 0.08f);
        particleGenerator1.setLifeError(0);
        particleGenerator1.setSpeedError(0.7f);
        particleGenerator1.setScaleError(0.5f);
    }

    private float sandStormStrength = 0;

    @Override
    public void update() {
        TestAliens aliens = (TestAliens)components.get(TestAliens.class).get(0);
        sandStormStrength = (float)( (aliens.getRound()-1)*0.5 + aliens.getArrayProgress()*0.5) ;

        generateSandStorm(sandStormStrength);

        terrain.get(3).update();
    }

    public void setSandStorm(float strength){
        this.sandStormStrength = strength;
    }

    private void generateSandStorm(float strength){
        if(strength>1){
            strength = 1;
        } else if (strength<0){
            strength = 0;
        }

        // red: 1.3 -> 0.4 , green: 1.2 -> 0.3 , blue: 1.2 -> 0.3
        float sunX = 1.3f - (strength*0.9f);
        float sunY = 1.2f - (strength*0.9f);
        float sunZ = 1.2f - (strength*0.9f);
        sun.setColor(new Vector3f(sunX, sunY, sunZ));

        //red: 0.2 -> 0.4 , green: 0.8 -> 0.3 , blue: 1 -> 0.2
        float skyX = 0.2f + (strength*0.2f);
        float skyY = 0.8f - (strength*0.5f);
        float skyZ = 1f - (strength*0.8f);
        LevelMain.setClearColor(skyX, skyY, skyZ);

        //'global' sand
        particleGenerator0.setDirection(new Vector3f(1f,0f,0f), 0.08f);
        particleGenerator0.setSpeed(600*strength);
        particleGenerator0.setPps(15*strength);
        particleGenerator0.setScale(150*strength);
        particleGenerator0.setLife(5/strength);
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-700,350,-700));
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-700,200,-700));
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-700,300,-400));
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-700,150,-400));
        particleGenerator0.setSpeed(400*strength);
        particleGenerator0.setLife(5/strength);
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-700,250,-300));
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-700,200,-100));
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-700,150,0));

        //'frontal' sand
        particleGenerator0.setSpeed(500*strength);
        particleGenerator0.setPps(20*strength);
        particleGenerator0.setScale(100*strength);
        particleGenerator0.setLife(4/strength);
        particleGenerator0.setDirection(new Vector3f(0.1f,0f,1f), 0.08f);
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(170,50,-700));
        particleGenerator0.setDirection(new Vector3f(-0.1f,0f,1f), 0.08f);
        ComponentAnimator.generateParticles(particleGenerator0, new Vector3f(-80,50,-700));
    }

    @Override
    public List<Entity> getEntityList() {
        return terrain;
    }

    @Override
    public Stream<Entity> getEntityStream() {
        return terrain.stream().map(x -> (Entity) x);
    }

    @Override
    public Light getSun() {
        return sun;
    }

    @Override
    public List<Light> getLights() {
        return Stream.concat(Stream.of(sun), Stream.of(lights).flatMap(Collection::stream))
                .collect(Collectors.toList());
    }

    @Override
    public Stream<Light> getLightStream() {
        return Stream.concat(Stream.of(sun), Stream.of(lights).flatMap(Collection::stream));
    }
}
