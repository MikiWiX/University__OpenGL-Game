package levelComponents;

import renderEngine.entities.Entity;

import java.util.List;
import java.util.stream.Stream;

public interface HasEntities {

    List<Entity> getEntityList();

    Stream<Entity> getEntityStream();

}
