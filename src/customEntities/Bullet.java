package customEntities;

import renderEngine.entities.Entity;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

public class Bullet extends Entity {

    public Bullet(Model model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale) {
        super(model, position, rotX, rotY, rotZ, scale);
    }

}
