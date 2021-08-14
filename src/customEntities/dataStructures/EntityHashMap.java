package customEntities.dataStructures;

import renderEngine.entities.Entity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityHashMap {

    private HashMap<EntityType, List<Entity>> entities = new HashMap<>();

    public enum EntityType {
        TERRAIN,
        ALIEN,
        SHIP,
        BULLET,
        OTHER
    }

    public void forEachEntity(EntityHashMapCallback callback) {
        safeEdit = false;
        arrayLoop(callback);
        safeEdit = true;
        //add all Entities that were put
        addEntities();
        removeEntities();
    }

    private void arrayLoop(EntityHashMapCallback callback) {
        Iterator<Map.Entry<EntityType, List<Entity>>> mapIterator = entities.entrySet().iterator();
        while(mapIterator.hasNext()) {
            EntityType listKey = mapIterator.next().getKey();
            List<Entity> list = entities.get(listKey);

            ListIterator<Entity> entityIterator = list.listIterator();
            while(entityIterator.hasNext()){
                Entity entity = entityIterator.next();

                if(!callback.call(entity, entityIterator, listKey)){
                    return;
                }

            }
            if(list.isEmpty()){
                mapIterator.remove();
            }
        }
    }

    // -- USAGE! --
    /*forEachEntity((entity, listIterator, listKey) -> {
        // You've passed a lambda!
        // forEachEntity() is done, do whatever you want here.
    });*/


    private volatile boolean safeEdit = true;

    private HashMap<EntityType, List<Entity>> entityAddBuffer = new HashMap<>();

    /**
     * puts entities into temporary hash map 'entityAddBuffer'
     * @param type Map Key
     * @param entity Entity
     */
    private void putEntity(EntityType type, Entity entity){
        List<Entity> list = entityAddBuffer.computeIfAbsent(type, k -> new ArrayList<>());
        list.add(entity);
    }
    private void putEntity(EntityType type, List<Entity> entity){
        List<Entity> list = entityAddBuffer.computeIfAbsent(type, k -> new ArrayList<>());
        list.addAll(entity);
    }
    /**
     * moves content from entityBuffer to entities
     */
    private void addEntities() {
        for(EntityType type : entityAddBuffer.keySet()){
            List<Entity> entity = entityAddBuffer.get(type);
            List<Entity> list = entities.computeIfAbsent(type, k -> new ArrayList<>());
            list.addAll(entity);
        }
        entityAddBuffer.clear();
    }
    /**
     * puts entities directly into hash map
     * @param type Map Key
     * @param entity Entity
     */
    public void addEntity(EntityType type, Entity entity){
        if(safeEdit){
            List<Entity> list = entities.computeIfAbsent(type, k -> new ArrayList<>());
            list.add(entity);
        } else {
            putEntity(type, entity);
        }

    }
    public void addEntity(EntityType type, List<Entity> entity){
        if(safeEdit){
            List<Entity> list = entities.computeIfAbsent(type, k -> new ArrayList<>());
            list.addAll(entity);
        } else {
            putEntity(type, entity);
        }
    }
    public void addEntity(EntityType type, Entity[] entity){
        addEntity(type, Arrays.asList(entity));
    }

    private HashMap<EntityType, List<Entity>> entityRemoveBuffer = new HashMap<>();

    /**
     * puts entities into temporary hash map 'entityRemoveBuffer'
     * @param type Map Key
     * @param entity Entity
     */
    private void dropEntity(EntityType type, Entity entity){
        List<Entity> list = entityRemoveBuffer.computeIfAbsent(type, k -> new ArrayList<>());
        list.add(entity);
    }
    private void dropEntity(EntityType type, List<Entity> entity){
        List<Entity> list = entityRemoveBuffer.computeIfAbsent(type, k -> new ArrayList<>());
        list.addAll(entity);
    }
    /**
     * removes content from entityBuffer in entities
     */
    private void removeEntities() {
        for(EntityType type : entityRemoveBuffer.keySet()){
            List<Entity> entity = entityRemoveBuffer.get(type);
            List<Entity> list = entities.get(type);
            if(list != null){
                list.removeAll(entity);
                if(list.isEmpty()){
                    entities.remove(type);
                }
            }
        }
        entityRemoveBuffer.clear();
    }
    /**
     * removes entities from hash map
     * @param type Map Key
     * @param entity Entity
     */
    public void removeEntity(EntityType type, Entity entity){
        if(safeEdit){
            List<Entity> list = entities.get(type);
            if(list==null) {
                return;
            }
            list.remove(entity);
            if(list.isEmpty()){
                entities.remove(type);
            }
        } else {
            dropEntity(type, entity);
        }
    }
    public void removeEntity(EntityType type, List<Entity> entity){
        if (safeEdit) {
            List<Entity> list = entities.get(type);
            if(list==null) {
                return;
            }
            list.removeAll(entity);
            if(list.isEmpty()){
                entities.remove(type);
            }
        } else {
            dropEntity(type, entity);
        }

    }

    public void lock() {
        while(!safeEdit) {
            Thread.onSpinWait();
        }
        safeEdit = false;
    }
    public void unlock() {
        safeEdit = true;
    }

    public HashMap<EntityType, List<Entity>> getHashMap() {
        return entities;
    }
    public List<Entity> getEntityList(){
        return entities
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    public Stream<Entity> getEntityStream(){
        return entities
                .values()
                .stream()
                .flatMap(Collection::stream);
    }
    public List<Entity> get(EntityType... keys){
        List<Entity> list = new ArrayList<>();
        for(EntityType key : keys){
            if(entities.get(key) != null){
                list.addAll( entities.get(key) );
            }
        }
        return list;
    }

}
