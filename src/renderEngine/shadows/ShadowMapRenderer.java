package renderEngine.shadows;

import Input.Settings;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import renderEngine.entities.cameras.Camera;
import renderEngine.entities.Light;
import renderEngine.entities.entityComponents.CmpAnimation;
import renderEngine.storage.Model;
import renderEngine.entities.Entity;
import org.lwjgl.opengl.GL11;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.storage.Vao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is in charge of using all of the classes in the renderEngine.shadows package to
 * carry out the shadow render pass, i.e. rendering the scene to the shadow map
 * texture. This is the only class in the renderEngine.shadows package which needs to be
 * referenced from outside the renderEngine.shadows package.
 * 
 * @author Karl
 *
 */
public class ShadowMapRenderer {

	private static int SHADOW_MAP_SIZE = Settings.SHADOW_RES;
	private static int SHADOW_MULTI_SAMPLING_QUALITY = Settings.SHADOW_MULTI_SAMPLING_QUALITY;

	private ShadowFrameBuffer shadowFbo;
	private ShadowShader shader;
	private ShadowBox shadowBox;
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f lightViewMatrix = new Matrix4f();
	private Matrix4f projectionViewMatrix = new Matrix4f();
	private Matrix4f offset = createOffset();

	/**
	 * Creates instances of the important objects needed for rendering the scene
	 * to the shadow map. This includes the {@link ShadowBox} which calculates
	 * the position and size of the "view cuboid", the simple renderer and
	 * shader program that are used to render objects to the shadow map, and the
	 * {@link ShadowFrameBuffer} to which the scene is rendered. The size of the
	 * shadow map is determined here.
	 * 
	 * @param camera
	 *            - the camera being used in the scene.
	 */
	public ShadowMapRenderer(Camera camera) {
		shader = new ShadowShader();
		shadowBox = new ShadowBox(lightViewMatrix, camera);
		shadowFbo = new ShadowFrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
	}

	/**
	 * Carries out the shadow render pass. This renders the renderEngine.entities to the
	 * shadow map. First the shadow box is updated to calculate the size and
	 * position of the "view cuboid". The light direction is assumed to be
	 * "-lightPosition" which will be fairly accurate assuming that the light is
	 * very far from the scene. It then prepares to render, renders the renderEngine.entities
	 * to the shadow map, and finishes rendering.
	 */
	private void NEWRender(Light sun) {
		shadowBox.update();
		Vector3f sunPosition = sun.getPosition();
		Vector3f lightDirection = new Vector3f(-sunPosition.x, -sunPosition.y, -sunPosition.z);
		prepare(lightDirection, shadowBox);
		render();
		finish();
	}

	public int renderShadowMap(List<Entity> entityList, Light sun) {
		for (Entity entity : entityList) {
			processEntity(entity);
		}
		NEWRender(sun);
		renderEntities.clear();
		return getShadowMap();
	}

	private Map<Model, List<Entity>> renderEntities = new HashMap<>();

	private void processEntity(Entity entity) {
		Model model = entity.getModel();
		List<Entity> batch = renderEntities.get(model);
		//if an entity using this model already exists
		if(batch!=null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			renderEntities.put(model, newBatch);
		}
	}

	/**
	 * Renders entieis to the shadow map. Each model is first bound and then all
	 * of the renderEngine.entities using that model are rendered to the shadow map.
	 */
	private void render() {
		for (Model model : renderEntities.keySet()) {
			Vao vao = model.getVao();
			vao.bind(0,4,5);
			for (Entity entity : renderEntities.get(model)) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, vao.getVertexCount(),
						GL11.GL_UNSIGNED_INT, 0);
			}
			vao.unbind(0,4,5);
		}
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Prepares an entity to be rendered. The model matrix is created in the
	 * usual way and then multiplied with the projection and view matrix (often
	 * in the past we've done this in the vertex shader) to create the
	 * mvp-matrix. This is then loaded to the vertex shader as a uniform.
	 *
	 * @param entity
	 *            - the entity to be prepared for rendering.
	 */
	private void prepareInstance(Entity entity) {
		Matrix4f modelMatrix = entity.getTransformationMatrix();
		//Matrix4f modelMatrix = Maths.createTransformationMatrix(entity.getPosition(),
		//		entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		shader.loadMvpMatrix(mvpMatrix);
		
		CmpAnimation cmpAni = entity.getComponent(CmpAnimation.class);
		boolean isAnim = cmpAni != null;
		shader.loadIsAnimated(isAnim);
		if(isAnim) {
			shader.loadJointTransforms(cmpAni.getJointTransforms());
		}
	}

	/**
	 * This biased projection-view matrix is used to convert fragments into
	 * "shadow map space" when rendering the main render pass. It converts a
	 * world space position into a 2D coordinate on the shadow map. This is
	 * needed for the second part of shadow mapping.
	 * 
	 * @return The to-shadow-map-space matrix.
	 */
	public Matrix4f getToShadowMapSpaceMatrix() {
		return Matrix4f.mul(offset, projectionViewMatrix, null);
	}

	public ShadowBox getShadowBox() {
		return shadowBox;
	}

	public int getShadowMapSize() {
		return SHADOW_MAP_SIZE;
	}

	public int getShadowMultiSamplingQuality() {
		return SHADOW_MULTI_SAMPLING_QUALITY;
	}

	/**
	 * Clean up the shader and renderEngine.FBO on closing.
	 */
	public void cleanUP() {
		shader.cleanUP();
		shadowFbo.cleanUp();
	}

	/**
	 * @return The ID of the shadow map texture. The ID will always stay the
	 *         same, even when the contents of the shadow map texture change
	 *         each frame.
	 */
	public int getShadowMap() {
		return shadowFbo.getShadowMap();
	}

	/**
	 * @return The light's "view" matrix.
	 */
	protected Matrix4f getLightSpaceTransform() {
		return lightViewMatrix;
	}

	/**
	 * Prepare for the shadow render pass. This first updates the dimensions of
	 * the orthographic "view cuboid" based on the information that was
	 * calculated in the {@link ShadowBox} class. The light's "view" matrix is
	 * also calculated based on the light's direction and the center position of
	 * the "view cuboid" which was also calculated in the {@link ShadowBox}
	 * class. These two matrices are multiplied together to create the
	 * projection-view matrix. This matrix determines the size, position, and
	 * orientation of the "view cuboid" in the world. This method also binds the
	 * renderEngine.shadows renderEngine.FBO so that everything rendered after this gets rendered to the
	 * renderEngine.FBO. It also enables depth testing, and clears any data that is in the
	 * FBOs depth attachment from last frame. The simple shader program is also
	 * started.
	 * 
	 * @param lightDirection
	 *            - the direction of the light rays coming from the sun.
	 * @param box
	 *            - the shadow box, which contains all the info about the
	 *            "view cuboid".
	 */
	private void prepare(Vector3f lightDirection, ShadowBox box) {
		updateOrthoProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength());
		updateLightViewMatrix(lightDirection, box.getCenter());
		Matrix4f.mul(projectionMatrix, lightViewMatrix, projectionViewMatrix);
		shadowFbo.bindFrameBuffer();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		shader.start();
	}

	/**
	 * Finish the shadow render pass. Stops the shader and unbinds the shadow
	 * renderEngine.FBO, so everything rendered after this point is rendered to the screen,
	 * rather than to the shadow renderEngine.FBO.
	 */
	private void finish() {
		shader.stop();
		shadowFbo.unbindFrameBuffer();
	}

	/**
	 * Updates the "view" matrix of the light. This creates a view matrix which
	 * will line up the direction of the "view cuboid" with the direction of the
	 * light. The light itself has no position, so the "view" matrix is centered
	 * at the center of the "view cuboid". The created view matrix determines
	 * where and how the "view cuboid" is positioned in the world. The size of
	 * the view cuboid, however, is determined by the projection matrix.
	 * 
	 * @param direction
	 *            - the light direction, and therefore the direction that the
	 *            "view cuboid" should be pointing.
	 * @param center
	 *            - the center of the "view cuboid" in world space.
	 */
	private void updateLightViewMatrix(Vector3f direction, Vector3f center) {
		direction.normalise();
		center.negate();
		lightViewMatrix.setIdentity();
		float pitch = (float) Math.acos(new Vector2f(direction.x, direction.z).length());
		Matrix4f.rotate(pitch, new Vector3f(1, 0, 0), lightViewMatrix, lightViewMatrix);
		float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
		yaw = direction.z > 0 ? yaw - 180 : yaw;
		Matrix4f.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0), lightViewMatrix,
				lightViewMatrix);
		Matrix4f.translate(center, lightViewMatrix, lightViewMatrix);
	}

	/**
	 * Creates the orthographic projection matrix. This projection matrix
	 * basically sets the width, length and height of the "view cuboid", based
	 * on the values that were calculated in the {@link ShadowBox} class.
	 * 
	 * @param width
	 *            - shadow box width.
	 * @param height
	 *            - shadow box height.
	 * @param length
	 *            - shadow box length.
	 */
	private void updateOrthoProjectionMatrix(float width, float height, float length) {
		projectionMatrix.setIdentity();
		projectionMatrix.m00 = 2f / width;
		projectionMatrix.m11 = 2f / height;
		projectionMatrix.m22 = -2f / length;
		projectionMatrix.m33 = 1;
	}

	/**
	 * Create the offset for part of the conversion to shadow map space. This
	 * conversion is necessary to convert from one coordinate system to the
	 * coordinate system that we can use to sample to shadow map.
	 * 
	 * @return The offset as a matrix (so that it's easy to apply to other matrices).
	 */
	private static Matrix4f createOffset() {
		Matrix4f offset = new Matrix4f();
		offset.translate(new Vector3f(0.5f, 0.5f, 0.5f));
		offset.scale(new Vector3f(0.5f, 0.5f, 0.5f));
		return offset;
	}
}
