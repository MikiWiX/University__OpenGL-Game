package renderEngine.particles;

import Input.Config;
import renderEngine.entities.cameras.Camera;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class Particle {

    private Vector3f position;
    private Vector3f velocity;
    private float gravityEffect;
    private float lifeLength;
    private float rotation;
    private float scale;

    private ParticleTexture texture;

    private Vector2f texOffset1 = new Vector2f();
    private Vector2f texOffset2 = new Vector2f();
    private float blend;

    private Vector3f tmpChange = new Vector3f();

    private float elapsedTime = 0;

    private float cameraDistance = 0;

    Random rand = new Random();

    protected Particle() {}

    protected Particle(ParticleMaster master, ParticleTexture texture, Vector3f position, Vector3f velocity, float gravityEffect,
                    float lifeLength, float rotation, float scale) {
        this.texture = texture;
        this.position = position;
        this.velocity = velocity;
        this.gravityEffect = gravityEffect;
        this.lifeLength = lifeLength;
        this.rotation = rotation;
        this.scale = scale;
        randomizeTextureXOffset();
        master.addParticle(this);
    }

    protected void setActive(ParticleMaster master, ParticleTexture texture, Vector3f position, Vector3f velocity, float gravityEffect,
                       float lifeLength, float rotation, float scale) {
        this.texture = texture;
        this.position = position;
        this.velocity = velocity;
        this.gravityEffect = gravityEffect;
        this.lifeLength = lifeLength;
        this.rotation = rotation;
        this.scale = scale;
        elapsedTime = 0;
        randomizeTextureXOffset();
        master.addParticle(this);
    }

    protected boolean update(Camera camera, float frameRenderTime) {
        velocity.y += Config.GRAVITY * gravityEffect * frameRenderTime;
        tmpChange.set(velocity);
        tmpChange.scale(frameRenderTime);
        Vector3f.add(tmpChange, position, position);
        updateCameraDistance(camera);
        updateTexture();
        elapsedTime += frameRenderTime;
        return (elapsedTime < lifeLength);
    }

    private void updateTexture() {
        float lifeFactor = elapsedTime/lifeLength;
        int stageCount = texture.getNumberOfCols();
        float atlasProgression = lifeFactor * stageCount;

        int index1 = (int) Math.floor(atlasProgression);
        int index2 = index1<stageCount-1 ? index1+1 : index1;
        this.blend = atlasProgression % 1;

        setTextureYOffset(texOffset1, index1);
        setTextureYOffset(texOffset2, index2);
    }

    private void setTextureYOffset(Vector2f offset, int index) {
        int nOc = texture.getNumberOfCols();

        offset.y = (float) index / nOc;

        //int column = index % nOc;
        //int row = index / nOr;

        //offset.x = (float) column / nOc;
        //offset.y = (float) row / nOr;
    }

    private void randomizeTextureXOffset() {
        int nOr = texture.getNumberOfRows();

        float tmp = (float) rand.nextInt(nOr) / nOr;
        texOffset1.x = tmp;
        texOffset2.x = tmp;
    }

    private void updateCameraDistance(Camera camera) {
        ///testing squared distance will give the same results but surprisingly this is more efficient
        cameraDistance = Vector3f.sub(camera.getPosition(), position, null).lengthSquared();
    }

    protected ParticleTexture getTexture() {
        return texture;
    }

    protected Vector3f getPosition() {
        return position;
    }

    protected float getRotation() {
        return rotation;
    }

    protected float getScale() {
        return scale;
    }

    protected Vector2f getTexOffset1() {
        return texOffset1;
    }

    protected Vector2f getTexOffset2() {
        return texOffset2;
    }

    protected float getBlend() {
        return blend;
    }

    public float getCameraDistance() {
        return cameraDistance;
    }
}
