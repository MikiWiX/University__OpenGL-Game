package renderEngine.shadows;

import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.renderer.ShaderProgram;

public class ShadowShader extends ShaderProgram {

	/**
	 * SET THOSE CONSTANTS SAME IN BOTH SHADERS !
	 */
	private static final int MAX_JOINTS = 50; //max joints allowed in a skeleton
	private static final int MAX_WEIGHTS = 3; //max number of joints that can affect a vertex
	
	private static final String VERTEX_FILE = "src/renderEngine/shadows/shadowVertexShader.glsl";
	private static final String FRAGMENT_FILE = "src/renderEngine/shadows/shadowFragmentShader.glsl";
	
	private int location_mvpMatrix;

	private int location_isAnimated;
	private int[] location_jointTransforms;

	protected ShadowShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocation() {
		location_mvpMatrix = super.getUniformLocation("mvpMatrix");

		location_isAnimated = super.getUniformLocation("isAnimated");
		location_jointTransforms = new int[MAX_JOINTS];
		for(int i=0; i<MAX_JOINTS; i++){
			location_jointTransforms[i] = super.getUniformLocation("jointTransforms[" + i + "]");
		}
		
	}
	
	protected void loadMvpMatrix(Matrix4f mvpMatrix){
		super.loadMatrix(location_mvpMatrix, mvpMatrix);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "in_position");
		super.bindAttribute(4, "in_jointIndices");
		super.bindAttribute(5, "in_weights");
	}

	protected void loadIsAnimated(boolean isAnimated) {
		super.loadBoolean(location_isAnimated, isAnimated);
	}
	protected void loadJointTransforms(Matrix4f[] transforms){
		for(int i=0;i<transforms.length;i++){
			super.loadMatrix(location_jointTransforms[i], transforms[i]);
		}
	}

}
