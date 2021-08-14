package levelComponents.aliens;

import customEntities.Alien;
import customEntities.dataStructures.AlienBlocks.PixelClassicAlienBlock;
import customEntities.dataStructures.AlienBlocks.SmoothClassicAlienBlock;
import levelComponents.HasEntities;
import levelComponents.LevelComponent;
import levelComponents.alienWeapons.TestProjectile;
import levelComponents.ships.TestShip;
import levelTemplate.LevelLoader;
import levelTemplate.LevelMain;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LoadedDataBuffer;
import renderEngine.GameMain;
import renderEngine.entities.Entity;
import renderEngine.entities.entityComponents.CmpHitBox;
import renderEngine.physics.hitBox.HitBoxAABB_DYNAMIC;
import renderEngine.physics.hitBox.HitBoxAABB_FIXED;
import renderEngine.physics.hitBox.HitBoxHULL;
import renderEngine.physics.hitBox.HitBoxOBB;
import renderEngine.storage.Loader;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static levelTemplate.LoadedDataBuffer.Type.ALIEN_MODEL;
import static levelTemplate.LoadedDataBuffer.Type.HIT_BOX;

public class TestAliens extends LevelComponent implements HasEntities {

    public List<Model> alienModels = new ArrayList<>();
    public List<HitBoxHULL> hitBoxes = new ArrayList<>();

    public List<PixelClassicAlienBlock> aliens = new ArrayList<>();

    public int round;
    //between 0 and 1, tells progress of destruction current array
    public float inRoundProgress;

    public TestAliens(ComponentMain levMain, List<LevelComponent> cmpList){
        super(levMain, cmpList);
    }
    public TestAliens(ComponentMain levMain, List<LevelComponent> cmpList, TestShip cmpShp){
        super(levMain, cmpList);
        addComponent(true, cmpShp);
    }
    public TestAliens(ComponentMain levMain, List<LevelComponent> cmpList, TestShip cmpShp, TestProjectile... cmpProj){
        super(levMain, cmpList);
        addComponent(true, cmpShp);
        addComponent(false, cmpProj);
    }

    @Override
    protected void loadFiles() {
        LoadedDataBuffer tmp11 = LevelLoader.loadModelFiles("aliens/skin1/Alien1Pos1", "test/Green", null, null);
        tmp11.ambient = 0.5f;
        tmp11.setTextureAnisotropic();
        tmp11.type = ALIEN_MODEL;
        put(tmp11);
        LoadedDataBuffer tmp21 = LevelLoader.loadModelFiles("aliens/skin1/Alien2Pos1", "test/Green", null, null);
        tmp21.ambient = 0.5f;
        tmp21.setTextureAnisotropic();
        tmp21.type = ALIEN_MODEL;
        put(tmp21);
        LoadedDataBuffer tmp31 = LevelLoader.loadModelFiles("aliens/skin1/Alien3Pos1", "test/Green", null, null);
        tmp31.ambient = 0.5f;
        tmp31.setTextureAnisotropic();
        tmp31.type = ALIEN_MODEL;
        put(tmp31);

        LoadedDataBuffer tmp12 = LevelLoader.loadModelFiles("aliens/skin1/Alien1Pos2", "test/Green", null, null);
        tmp12.ambient = 0.5f;
        tmp12.setTextureAnisotropic();
        tmp12.type = ALIEN_MODEL;
        put(tmp12);
        LoadedDataBuffer tmp22 = LevelLoader.loadModelFiles("aliens/skin1/Alien2Pos2", "test/Green", null, null);
        tmp22.ambient = 0.5f;
        tmp22.setTextureAnisotropic();
        tmp22.type = ALIEN_MODEL;
        put(tmp22);
        LoadedDataBuffer tmp32 = LevelLoader.loadModelFiles("aliens/skin1/Alien3Pos2", "test/Green", null, null);
        tmp32.ambient = 0.5f;
        tmp32.setTextureAnisotropic();
        tmp32.type = ALIEN_MODEL;
        put(tmp32);

        //HIT_BOXES
        LoadedDataBuffer hb1 = LevelLoader.loadHitBox("aliens/skin1/HitBox1", HitBoxOBB.class);
        hb1.type = HIT_BOX;
        put(hb1);
        LoadedDataBuffer hb2 = LevelLoader.loadHitBox("aliens/skin1/HitBox2", HitBoxOBB.class);
        hb2.type = HIT_BOX;
        put(hb2);
        LoadedDataBuffer hb3 = LevelLoader.loadHitBox("aliens/skin1/HitBox3", HitBoxOBB.class);
        hb3.type = HIT_BOX;
        put(hb3);
    }

    @Override
    protected void loadToOpenGL(Loader loader, LoadedDataBuffer pm) {
        switch (pm.type){
            case ALIEN_MODEL:
                alienModels.add(LevelMain.loadAsObject(loader, pm));
                break;
            case HIT_BOX:
                hitBoxes.add(pm.hitBox);
        }

    }

    @Override
    public void init() {
        nextRound();
    }

    private int[] arraySize = new int[] {5,10, 5*10 };
    private float stepRate = 0.014f;

    private void nextRound() {
        round++;

        Alien[][] al = new Alien[ arraySize[0] ][ arraySize[1] ];
        for (int i = 0; i < arraySize[0]; i++) {
            for (int j = 0; j < arraySize[1]; j++) {
                Alien alien = new Alien(alienModels.get(i/2), new Vector3f(0), 0, 0, 0, new Vector3f(0.7f));
                alien.addModel(alienModels.get((i/2)+3));
                alien.addComponent(new CmpHitBox(hitBoxes.get(i/2)));
                alien.pickModel(0);
                al[i][j] = alien;
            }
        }
        PixelClassicAlienBlock ab = new PixelClassicAlienBlock(al, new Vector3f(0, 150, 50), 200, 80, SmoothClassicAlienBlock.AlienBlockPoint.CENTER);

        if(aliens.size()==0){
            aliens.add(ab);
        } else {
            aliens.set(0, ab);
        }

        aliens.get(0).setMovement(2f, -120, 120, 5);
        stepRate -= stepRate/3;
        if(stepRate == 0){
            stepRate = Float.MIN_VALUE;
        }
        aliens.get(0).setStepRate( stepRate );

        averageProjectilesCoolDown -= averageProjectilesCoolDown /3;
    }

    public float getArrayProgress() {
        return ( (float) arraySize[2] - aliens.get(0).getAlienList().size()) / arraySize[2];
    }

    public int getRound() {
        return round;
    }

    private float respawnTimer = 0;
    private float averageProjectilesCoolDown = 3;

    @Override
    public void update() {
        aliens.get(0).update();

        alienShoots(averageProjectilesCoolDown);

        if(aliens.get(0).isEmpty()){
            if(((TestShip) components.get(TestShip.class).get(0)).getLives()>0){
                respawnTimer += GameMain.getFrameRenderTime();
                if(respawnTimer > 2){
                    nextRound();
                    respawnTimer = 0;
                }
            }
        }
    }

    float timeTillNextShoot = 0f;

    private void alienShoots(float averageProjectilesCoolDown) {
        if( ((TestShip) components.get(TestShip.class).get(0)).shipIsDead() || aliens.get(0).isEmpty()){
            return;
        }

        timeTillNextShoot -= GameMain.getFrameRenderTime();
        if(timeTillNextShoot < 0){

            Random rand = new Random();

            timeTillNextShoot += rand.nextFloat()*2*averageProjectilesCoolDown;

            //shoot
            List<Alien> alienList = aliens.get(0).getAlienList();
            Alien shootingAlien = alienList.get(rand.nextInt(alienList.size()));
            ( (TestProjectile)components.get(TestProjectile.class).get(0) ).fire(shootingAlien);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> getEntityList() {
        return (List<Entity>) (List<?>) aliens.get(0).getAlienList();
    }

    @Override
    public Stream<Entity> getEntityStream() {
        return aliens.get(0).getAlienStream().map(x -> x);
    }
}
