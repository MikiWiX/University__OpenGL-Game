package customEntities.dataStructures.AlienBlocks;

import customEntities.Alien;

public interface AlienBlockCallback {

    //return TRUE if continue loop
    boolean call(Alien alien, int i, int j);
}
