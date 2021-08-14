package renderEngine.renderer;

import Input.Settings;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import renderEngine.GameMain;
import renderEngine.entities.cameras.Camera;
import renderEngine.entities.Entity;
import renderEngine.entities.Light;
import renderEngine.entities.entityComponents.CmpAnimation;
import renderEngine.storage.Model;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.storage.Vao;
import renderEngine.shadows.ShadowMapRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE7;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class EntityRenderer {


    private EntityShader shader = new EntityShader();

    private ShadowMapRenderer shadowMapRenderer;
    // hash map containing all textured models and renderEngine.entities needed for a given frame

    private Map<Model, List<Entity>> normalMapEntities = new HashMap<Model, List<Entity>>();

    private Matrix4f projectionMatrix;

    public EntityRenderer(Camera cam) {
        /* those two will prevent system from rendering hidden triangles, CULL = not render */
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        projectionMatrix = cam.createProjectionMatrix();

        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.connectTextureUnits();
        shader.stop();
    }

    public void bindShadows(ShadowMapRenderer shadowRenderer) {
        shadowMapRenderer = shadowRenderer;
    }

    public void unbindShadows() {
        if(shadowMapRenderer != null) {
            shadowMapRenderer.cleanUP();
        }
        shadowMapRenderer = null;
    }

    private void prepareShader(List<Light> lights, Matrix4f viewMatrix) {
        shader.loadViewMatrix(viewMatrix);
        shader.loadLights(lights, viewMatrix);

        boolean useShadows = (Settings.SHADOWS_ON && shadowMapRenderer != null);
        shader.loadUseShadow(useShadows);
        if(useShadows) {
            shader.loadShadowVariables(
                    shadowMapRenderer.getToShadowMapSpaceMatrix(),
                    shadowMapRenderer.getShadowBox().getShadowDistance(),
                    shadowMapRenderer.getShadowMultiSamplingQuality(),
                    shadowMapRenderer.getShadowMapSize(),
                    Settings.SHADOW_TRANSITION_WIDTH,
                    Settings.SHADOW_TRANSITION_OFFSET);
        }
    }

    public void processEntity(Entity entity) {
        Model model = entity.getModel();
        List<Entity> batch = normalMapEntities.get(model);
        //if an entity using this model already exists
        if(batch!=null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            normalMapEntities.put(model, newBatch);
        }
    }

    public void prepare(int shadowMap) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, GameMain.getInWidth(), GameMain.getInHeight());
        glActiveTexture(GL_TEXTURE7);
        glBindTexture(GL_TEXTURE_2D, shadowMap);
    }

    public void render(List<Light> lights, Camera camera, int shadowMap) {
        prepare(shadowMap);
        Matrix4f viewMatrix = camera.createViewMatrix();

        //render for normal mapped, packed inside a function
        shader.start();
        prepareShader(lights, viewMatrix);
        drawEntities();
        shader.stop();

        normalMapEntities.clear();
    }

    public void drawEntities(/*Vector4f clipPlane,*/) {
        //prepare(/*clipPlane,*/ lights, camera);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        for (Model model : normalMapEntities.keySet()) {
            prepareTexturedModel(model);
            List<Entity> batch = normalMapEntities.get(model);
            for (Entity entity : batch) {
                prepareInstance(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVao().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
            }
            unbindTexturedModel(model);
        }
        //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    public void resetProjectionMatrix(Matrix4f pm) {
        projectionMatrix = pm;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    public void cleanUP(){
        shader.cleanUP();
        if(shadowMapRenderer != null) {
            shadowMapRenderer.cleanUP();
        }
    }

    private void prepareTexturedModel(Model model) {
        Vao vao = model.getVao();
        vao.bind(0,1,2,3,4,5);
        //GL11.glEnable(GL11.GL_BLEND);
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        shader.loadNumberOfRows(model.getNumberOfRows());
        /* that is for transparency, I don't use it
        if (texture.isHasTransparency()) {
            MasterRenderer.disableCulling();
        }
        */
        boolean hasTexture = model.hasTexture();
        if(hasTexture) {
            boolean hasNormalMap = model.hasNormalMap();
            boolean hasSpecularMap = model.hasSpecularMap();

            shader.loadTextureBooleans(true, hasNormalMap, hasSpecularMap);

            shader.loadTexturesScales(model.getTextureScale(), model.getNormalScale(), model.getSpecularScale());

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture());

            if (hasNormalMap) {
                GL13.glActiveTexture(GL13.GL_TEXTURE1);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getNormalMap());
            }

            if (hasSpecularMap) {
                GL13.glActiveTexture(GL13.GL_TEXTURE2);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getSpecularMap());
            }
        } else {
            shader.loadTextureBooleans(false, false, false);
        }
    }

    private void unbindTexturedModel(Model model) {
        // transparency handling commented below
        // MasterRenderer.enableCulling();
        //GL11.glDisable(GL11.GL_BLEND);
        Vao vao = model.getVao();
        vao.unbind(0,1,2,3,4,5);
    }

    private void prepareInstance(Entity entity) {
        Matrix4f transformationMatrix = entity.getTransformationMatrix();
        shader.loadTransformationMatrix(transformationMatrix);
        shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());

        shader.loadShineVariables(entity.getShineDamper(), entity.getReflectivity(), entity.getAmbient());

        CmpAnimation cmpAni = entity.getComponent(CmpAnimation.class);
        boolean hasAnimation = cmpAni != null;
        shader.loadAnimationBoolean(hasAnimation);
        if (hasAnimation) {
            shader.loadJointTransforms(cmpAni.getJointTransforms());
        }
    }

    private void prepare(/*Vector4f clipPlane,*/ List<Light> lights, Camera camera) {
        //shader.loadClipPlane(clipPlane);
        //need to be public variables in MasterRenderer
        //shader.loadSkyColour(MasterRenderer.RED, MasterRenderer.GREEN, MasterRenderer.BLUE);
    }
}
