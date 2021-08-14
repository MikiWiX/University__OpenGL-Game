package levelComponents.ships;

import Input.Input;
import customEntities.Ship;
import customEntities.dataStructures.AlienBlocks.PixelClassicAlienBlock;
import customEntities.dataStructures.EntityHashMap;
import customEntities.dataStructures.AlienBlocks.SmoothClassicAlienBlock;
import levelComponents.HasEntities;
import levelComponents.HasParticles;
import levelComponents.LevelComponent;
import levelComponents.aliens.TestAliens;
import levelComponents.weapons.TestRocket;
import levelTemplate.LevelLoader;
import levelTemplate.LevelMain;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LoadedDataBuffer;
import renderEngine.GameMain;
import renderEngine.entities.Entity;
import renderEngine.entities.entityComponents.CmpHitBox;
import renderEngine.particles.ParticleMaster;
import renderEngine.particles.ParticleSystem;
import renderEngine.particles.ParticleTexture;
import renderEngine.physics.CollisionDetector;
import renderEngine.physics.hitBox.HitBoxAABB_DYNAMIC;
import renderEngine.physics.hitBox.HitBoxAABB_FIXED;
import renderEngine.physics.hitBox.HitBoxHULL;
import renderEngine.physics.hitBox.HitBoxOBB;
import renderEngine.storage.Loader;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static customEntities.dataStructures.EntityHashMap.EntityType.SHIP;
import static levelTemplate.LoadedDataBuffer.Type.*;

public class TestShip extends LevelComponent implements HasEntities, HasParticles {

    private Model shipModel;

    public EntityHashMap entities = new EntityHashMap();
    public List<HitBoxHULL> hitBoxes = new ArrayList<>();

    private ParticleSystem particleGenerator;
    private ParticleTexture particleTexture;

    public List<TestRocket> weapons = new ArrayList<>();

    public TestShip(ComponentMain levMain, List<LevelComponent> cmpList){
        super(levMain, cmpList);
    }
    public TestShip(ComponentMain levMain, List<LevelComponent> cmpList, TestRocket... cmpWpn){
        super(levMain, cmpList);
        addWeapon(cmpWpn);
    }

    //@SafeVarargs
    public final void addWeapon(TestRocket... components){
        for(TestRocket component : components){
            //add component here
            this.weapons.add(component);

            //add this to the component
            component.ship = this;
        }
    }

    @Override
    protected void loadFiles() {
        LoadedDataBuffer tmp = LevelLoader.loadModelFiles("test/cube", "test/Gray", null, null);
        tmp.ambient = 0.5f;
        tmp.setTextureAnisotropic();
        tmp.type = SHIP_MODEL;
        put(tmp);

        LoadedDataBuffer tex0 = LevelLoader.loadImageFile("starAtlas");
        tex0.numberOfTextureCols = 2;
        tex0.numberOfTextureRows = 2;
        //tex0.alphaBlending = true;
        tex0.type = PARTICLE;
        put(tex0);

        //HIT_BOXES
        LoadedDataBuffer hb0 = LevelLoader.loadHitBox("test/cube", HitBoxOBB.class);
        hb0.type = HIT_BOX;
        put(hb0);
    }

    @Override
    protected void loadToOpenGL(Loader loader, LoadedDataBuffer pm) {
        switch (pm.type){
            case SHIP_MODEL:
                shipModel = LevelMain.loadAsObject(loader, pm);
                break;
            case PARTICLE:
                particleTexture = new ParticleTexture(LevelMain.loadAsImage(loader, pm), pm.numberOfTextureCols, pm.numberOfTextureRows, pm.alphaBlending);
                break;
            case HIT_BOX:
                hitBoxes.add(pm.hitBox);
        }
    }

    @Override
    public void init() {
        Ship ship = new Ship(shipModel, new Vector3f(0, 25f, 48f), 0, 0, 0, new Vector3f(8));
        ship.addComponent(new CmpHitBox(hitBoxes.get(0)));
        entities.addEntity(SHIP, ship);
    }

    @Override
    public void spawnParticleSystems(ParticleMaster renderer){
        particleGenerator = new ParticleSystem(renderer, particleTexture, 500, 160, 20, 2, 4);
        particleGenerator.randomizeRotation(0);
        particleGenerator.addRotation(90);
        particleGenerator.setDirection(new Vector3f(-0.2f,1,-0.1f), 0.08f);
        particleGenerator.setLifeError(0);
        particleGenerator.setSpeedError(0.5f);
        particleGenerator.setScaleError(0.5f);
    }

    int lives = 3;
    float endTimer = 0;
    float shipDeadPenalty = 0;
    float shipImmortality = 0;

    @Override
    public void update() {

        if(shipImmortality < 0 ){
            shipImmortality = 0;
        } else if(shipImmortality > 0){
            shipImmortality -= GameMain.getFrameRenderTime();
        }

        List<Entity> ships = entities.get(SHIP);
        if(ships == null || ships.isEmpty()){
            shipDeadPenalty += GameMain.getFrameRenderTime();
            if(shipDeadPenalty > 3 && lives>0){
                Ship s0 = new Ship(shipModel, new Vector3f(0, 25f, 48f), 0, 0, 0, new Vector3f(8));
                s0.addComponent(new CmpHitBox(hitBoxes.get(0)) );
                entities.addEntity(SHIP, s0);
                shipDeadPenalty = 0;
                shipImmortality = 2;
            }
        }

        entities.forEachEntity((entity, entityIterator, listKey) -> {
            Ship ship = (Ship) entity;
            ship.move(85, Input.getMoveShipLeft(), Input.getMoveShipRight());
            ship.alignToBorders(-120, 120);
            ship.update();

            if (Input.getHoldShoot()) {
                weapons.get(0).fire();
            }

            for(LevelComponent aln : components.get(TestAliens.class)){

                //check for collision with aliens
                for (PixelClassicAlienBlock ab : ((TestAliens)aln).aliens ) {
                    ab.forEachEntity((alien, i, j) -> {
                        if(CollisionDetector.intersectionTest(ship, alien)) {
//                            entityIterator.remove();
//                            lives--;
                            destroyShip();
                            ab.remove(i,j);
                            ab.resetPos(new Vector3f(0, 150, 48), SmoothClassicAlienBlock.AlienBlockPoint.CENTER);
                            return false; //break the loop
                        }
                        return true;
                    });
                }

            }
            return true;
        });

//        if(Input.getSpawnPart()){
//            RegularAnimator.generateParticles(particleGenerator, entities.get(SHIP).get(0).getPosition());
//        }

        if(lives <= 0){
            endTimer += GameMain.getFrameRenderTime();
            if(endTimer > 5){
                levMain.exitLevel();
            }
        }
    }

    public boolean shipIsDead() {
        return shipDeadPenalty>0;
    }
    public int getLives() {
        return lives;
    }
    public void destroyShip() {
        if(shipImmortality <= 0){
            lives--;
            entities.removeEntity(SHIP, entities.get(SHIP).get(0));

            for(LevelComponent aln : components.get(TestAliens.class)) {
                for (PixelClassicAlienBlock ab : ((TestAliens) aln).aliens) {
                    ab.resetPos(new Vector3f(0, 150, 48), SmoothClassicAlienBlock.AlienBlockPoint.CENTER);
                }
            }
        }
    }

    @Override
    public List<Entity> getEntityList() {
        return entities.get(SHIP);
    }

    @Override
    public Stream<Entity> getEntityStream() {
        List<Entity> list = getEntityList();
        return list==null ? null : list.stream();
    }

}
