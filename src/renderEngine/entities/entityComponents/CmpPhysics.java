package renderEngine.entities.entityComponents;

import renderEngine.GameMain;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

public class CmpPhysics extends CmpInterface {

    private Vector3f velocity;
    private Vector3f momentum;

    private float gravityAffection;

    public CmpPhysics (float gravityAffection, Vector3f velocity, Vector3f momentum) {
        this.gravityAffection = gravityAffection;
        this.velocity = velocity;
        this.momentum = momentum;
    }

    @Override
    public void update() {
        float ft = GameMain.getFrameRenderTime();
        velocity.y -= gravityAffection * ft;
        entity.increasePosition(velocity.x, velocity.y, velocity.z);
    }
}
