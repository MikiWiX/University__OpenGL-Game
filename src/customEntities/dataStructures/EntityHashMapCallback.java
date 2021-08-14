package customEntities.dataStructures;

import renderEngine.entities.Entity;

import java.util.ListIterator;

public interface EntityHashMapCallback {

    boolean call(Entity entity, ListIterator<Entity> entityIterator, EntityHashMap.EntityType listKey);
}
