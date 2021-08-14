package renderEngine.entities.entityComponents;

import renderEngine.entities.Entity;

public abstract class CmpInterface {

    protected Entity entity;

    /**
     * set parent entity in component. Called right after constructor.
     * @param entity entity that poses component instance
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    };

    /**
     * optional function called after setting entity
     */
    public void init(){};

    /**
     * gets called once every frame (at the beginning)
     */
    public abstract void update();
}
