package renderEngine.entities.entityComponents;

import renderEngine.physics.hitBox.*;
import renderEngine.toolbox.Maths;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.List;

public class CmpHitBox extends CmpInterface {

    /**
     * srcHitBox - loaded hitBox (with model)
     * worldHitBox - hitBox after transformation (to entity)
     */
    private HitBoxHULL srcHitBox;
    private HitBoxHULL worldHitBox;

    private Matrix4f rotationMatrix;

    public CmpHitBox(HitBoxHULL hb){
        this.srcHitBox = hb;
    }

    @Override
    public void init() {}

    @Override
    public void update() {
        worldHitBox = null;
    }

    public HitBoxHULL getWorldSpaceHitBox() {
        if(worldHitBox == null){
            entity.updateTransformationMatrix();
            updateRotationMatrix();
            worldHitBox = srcHitBox.createWorldHitBox(entity.getTransformationMatrix(), rotationMatrix, entity.getScale(), entity.getPosition());
        }
        return worldHitBox;
    }

    public void updateRotationMatrix() {
        this.rotationMatrix = Maths.createRotationMatrix4f(entity.getRotX(), entity.getRotY(), entity.getRotZ());
    }

    public HitBoxHULL getSrcHitBox() {
        return srcHitBox;
    }

    public Matrix4f getRotationMatrix() {
        return rotationMatrix;
    }
}
