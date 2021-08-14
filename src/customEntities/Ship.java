package customEntities;

import Input.Input;
import renderEngine.entities.entityComponents.CmpHitBox;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.entities.Entity;

public class Ship extends Entity {

    public Ship(Model model, Vector3f position, float rotX, float rotY, float rotZ, Vector3f scale) {
        super(model, position, rotX, rotY, rotZ, scale);
    }

    public void move(float speed, boolean moveLeft, boolean moveRight) {
        if (moveLeft) {
            increasePosition(-speed,0,0);
        }
        if (moveRight) {
            increasePosition(speed,0,0);
        }
    }

    public void alignToBorders(float minX, float maxX) {
        if(getPosition().x < minX){
            getPosition().x = minX;
        } else if(getPosition().x > maxX){
            getPosition().x = maxX;
        }
    }
}
