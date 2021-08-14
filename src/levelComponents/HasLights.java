package levelComponents;

import renderEngine.entities.Light;

import java.util.List;
import java.util.stream.Stream;

public interface HasLights {

    Light getSun();
    List<Light> getLights();
    Stream<Light> getLightStream();
}
