package levelComponents.alienWeapons;

import customEntities.Bullet;
import customEntities.dataStructures.EntityHashMap;
import levelComponents.HasEntities;
import levelComponents.LevelComponent;
import levelComponents.ships.TestShip;
import levelTemplate.LevelLoader;
import levelTemplate.LevelMain;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LoadedDataBuffer;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static customEntities.dataStructures.EntityHashMap.EntityType.BULLET;
import static customEntities.dataStructures.EntityHashMap.EntityType.SHIP;
import static levelTemplate.LoadedDataBuffer.Type.ALIEN_MODEL;
import static levelTemplate.LoadedDataBuffer.Type.HIT_BOX;

public class TestProjectile extends LevelComponent implements HasEntities {

    protected EntityHashMap entities = new EntityHashMap();
    public List<HitBoxHULL> hitBoxes = new ArrayList<>();

    private Model bulletModel;

    private TestShip shipComponent;

    public TestProjectile(ComponentMain levMain, List<LevelComponent> cmpList){
        super(levMain, cmpList);
    }
    public TestProjectile(ComponentMain levMain, List<LevelComponent> cmpList, TestShip cmpShp){
        super(levMain, cmpList);
        shipComponent = cmpShp;
    }

    @Override
    protected void loadFiles() {
        LoadedDataBuffer tmp2 = LevelLoader.loadModelFiles("test/cube", "test/Green", null, null);
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
    public void init() {

    }

    @Override
    public void update() {
        entities.forEachEntity((entity, entityIterator, listKey) -> {
            Bullet projectile = (Bullet) entity;
            entity.increasePosition(0, -50, 0);

            projectile.update();

            if (projectile.getPosition().y < -20) {
                entityIterator.remove();
                return true;
            }

            List<EntityHashMap> bm = shipComponent
                    .weapons
                    .stream()
                    .map(x -> x.entities)
                    .collect(Collectors.toList());

            for (EntityHashMap bulletMap : bm ) {
                bulletMap.forEachEntity((bullet, bulletIterator, bulletListKey) -> {
                    if( CollisionDetector.intersectionTest(projectile, bullet) ){
                        bulletIterator.remove();
                    }
                    return true;
                });
            }

            for(Entity shp : shipComponent.entities.get(SHIP)){
                if ( CollisionDetector.intersectionTest(projectile, shp) ) {
                    shipComponent.destroyShip();
                    entityIterator.remove();
                    return false;
                }
            }

            return true;
        });
    }

    public void fire(Vector3f position) {
        Bullet b = new Bullet(bulletModel, position, 0, 0, 0, new Vector3f(2));
        //b.addComponent(new CmpHitBox(levMain.hitBoxes.get(levMain.rand.nextInt(4))));
        b.addComponent(new CmpHitBox(hitBoxes.get(0)));
        entities.addEntity(BULLET, b);
    }
    public void fire(Entity alien) {
        fire(alien.copyPosition());
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
