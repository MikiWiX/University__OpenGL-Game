package levelComponents.weapons;

import customEntities.Bullet;
import customEntities.Ship;
import customEntities.dataStructures.AlienBlocks.PixelClassicAlienBlock;
import customEntities.dataStructures.EntityHashMap;
import levelComponents.HasEntities;
import levelComponents.LevelComponent;
import levelComponents.aliens.TestAliens;
import levelComponents.ships.TestShip;
import levelTemplate.LevelLoader;
import levelTemplate.LevelMain;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LoadedDataBuffer;
import renderEngine.GameMain;
import renderEngine.entities.Entity;
import renderEngine.entities.entityComponents.CmpHitBox;
import renderEngine.physics.CollisionDetector;
import renderEngine.physics.hitBox.HitBoxAABB_FIXED;
import renderEngine.physics.hitBox.HitBoxHULL;
import renderEngine.physics.hitBox.HitBoxOBB;
import renderEngine.storage.Loader;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static customEntities.dataStructures.EntityHashMap.EntityType.BULLET;
import static customEntities.dataStructures.EntityHashMap.EntityType.SHIP;
import static levelTemplate.LoadedDataBuffer.Type.ALIEN_MODEL;
import static levelTemplate.LoadedDataBuffer.Type.HIT_BOX;

public class TestRocket extends LevelComponent implements HasEntities {

    public EntityHashMap entities = new EntityHashMap();
    public List<HitBoxHULL> hitBoxes = new ArrayList<>();

    private Model bulletModel;

    public TestShip ship;

    public TestRocket(ComponentMain levMain, List<LevelComponent> cmpList){
        super(levMain, cmpList);
    }
    public TestRocket(ComponentMain levMain, List<LevelComponent> cmpList, TestShip ship){
        super(levMain, cmpList);
        this.ship = ship;
    }

    @Override
    protected void loadFiles() {
        LoadedDataBuffer tmp2 = LevelLoader.loadModelFiles("test/cube", "test/Gray", null, null);
        tmp2.ambient = 0.5f;
        tmp2.setTextureAnisotropic();
        tmp2.type = ALIEN_MODEL;
        put(tmp2);

        //hitBox
        LoadedDataBuffer hb1 = LevelLoader.loadHitBox("test/cube", HitBoxOBB.class);
        hb1.type = HIT_BOX;
        put(hb1);
    }

    @Override
    protected void loadToOpenGL(Loader loader, LoadedDataBuffer pm) {
        switch (pm.type){
            case ALIEN_MODEL:
                bulletModel = LevelMain.loadAsObject(loader, pm);
                break;
            case HIT_BOX:
                hitBoxes.add(pm.hitBox);
        }
    }

    @Override
    public void init() {}

    @Override
    public void update() {
        entities.forEachEntity((entity, entityIterator, listKey) -> {
            Bullet bullet = (Bullet) entity;
            entity.increasePosition(0, 150, 0);
            bullet.update();

            if (bullet.getPosition().y > 320) {
                entityIterator.remove();
                return true;
            }

            for (LevelComponent aln : ship.components.get(TestAliens.class)) {

                //check for collision with aliens
                for (PixelClassicAlienBlock ab : ((TestAliens) aln).aliens) {
                    ab.forEachEntity((alien, i, j) -> {
                        if (CollisionDetector.intersectionTest(bullet, alien)) {
                            entityIterator.remove();
                            ab.remove(i, j);
                            return false; //break the loop
                        }
                        return true;
                    });
                }

            }
            return true;
        });

        coolDown -= GameMain.getFrameRenderTime();
        if(coolDown < 0){
            coolDown = 0;
        }
    }

    private float coolDown = 0;

    public void fire(Ship ship) {
        if(coolDown <= 0 || entities.get(BULLET).isEmpty()) {
            Bullet b = new Bullet(bulletModel, ship.copyPosition(), 0, 0, 0, new Vector3f(2));
            //b.addComponent(new CmpHitBox(levMain.hitBoxes.get(levMain.rand.nextInt(4))));
            b.addComponent(new CmpHitBox(hitBoxes.get(0)));
            entities.addEntity(BULLET, b);

            coolDown = 0.5f;
        }
    }
    public void fire() {
        fire((Ship) ship.entities.get(SHIP).get(0));
    }

    @Override
    public List<Entity> getEntityList() {
        return entities.get(BULLET);
    }

    @Override
    public Stream<Entity> getEntityStream() {
        List<Entity> list = getEntityList();
        return list==null ? null : list.stream();
    }
}
