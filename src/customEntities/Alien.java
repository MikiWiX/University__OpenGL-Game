package customEntities;


import renderEngine.GameMain;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.entities.Entity;

public class Alien extends Entity {

    public Alien(Model model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale) {
        super(model, position, rotX, rotY, rotZ, scale);
    }
}
