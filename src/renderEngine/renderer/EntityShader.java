package renderEngine.renderer;

import renderEngine.entities.Light;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector4f;

import java.util.List;

public class EntityShader extends ShaderProgram{

    /**
     * SET THOSE CONSTANTS SAME IN BOTH SHADERS !
     */
    private static final int MAX_LIGHTS = 10; //max number of lights
    private static final int MAX_JOINTS = 50; //max joints allowed in a skeleton
    private static final int MAX_WEIGHTS = 3; //max number of joints that can affect a vertex

    private static final String VERTEX_FILE = "src/renderEngine/renderer/universalVShader.glsl";
    private static final String FRAGMENT_FILE = "src/renderEngine/renderer/universalFShader.glsl";

    //private int location_skyColour;
    //private int location_plane;
    private int location_modelTexture;
    private int location_normalMap;
    private int location_specularMap;
    private int location_shadowMap;

    //loads when shader starts / resets
    private int location_projectionMatrix;

    //load at the beginning of rendering
    private int location_viewMatrix;
    private int[] location_lightPositionEyeSpace;
    private int[] location_lightColour;
    private int[] location_attenuation;
        private int location_useShadowMap;
        //if shadows are on
        private int location_toShadowMapSpace;
        private int location_shadowDistance;
        private int location_shadowMultiSampling;
        private int location_mapSize;
        private int location_shadowTransitionWidth;
        private int location_shadowTransitionOffset;

    //load per model
    private int location_numberOfTextureRows;
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_ambient;
        private int location_useTexture;
        private int location_useNormalMap;
        private int location_useSpecularMap;
        private int location_useAnimation;
        //if has texture
        private int location_textureMultiplier;
        private int location_normalMultiplier;
        private int location_specularMultiplier;
        //if has animation
        private int[] location_jointTransforms;

    //load per entity
    private int location_transformationMatrix;
    private int location_offset;


    public EntityShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
        super.bindAttribute(3, "tangent");
        super.bindAttribute(4, "in_jointIndices");
        super.bindAttribute(5, "in_weights");
        //6 - FREE
        //7 - renderEngine.shadows
    }

    @Override
    protected void getAllUniformLocation() {
        //location_skyColour = super.getUniformLocation("skyColour");
        //location_plane = super.getUniformLocation("plane");
        location_modelTexture = super.getUniformLocation("modelTexture");
        location_normalMap = super.getUniformLocation("normalMap");
        location_specularMap = super.getUniformLocation("specularMap");
        location_shadowMap = super.getUniformLocation("shadowMap");

        location_projectionMatrix = super.getUniformLocation("projectionMatrix");

        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_lightPositionEyeSpace = new int[MAX_LIGHTS];
        location_lightColour = new int[MAX_LIGHTS];
        location_attenuation = new int[MAX_LIGHTS];
        for(int i=0;i<MAX_LIGHTS;i++){
            location_lightPositionEyeSpace[i] = super.getUniformLocation("lightPositionEyeSpace[" + i + "]");
            location_lightColour[i] = super.getUniformLocation("lightColour[" + i + "]");
            location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
        }
        location_useShadowMap = super.getUniformLocation("useShadowMap");

        location_toShadowMapSpace = super.getUniformLocation("toShadowMapSpace");
        location_shadowDistance = super.getUniformLocation("shadowDistance");
        location_shadowMultiSampling = super.getUniformLocation("pcfCount");
        location_mapSize = super.getUniformLocation("mapSize");
        location_shadowTransitionWidth = super.getUniformLocation("transitionWidth");
        location_shadowTransitionOffset = super.getUniformLocation("transitionOffset");

        location_numberOfTextureRows = super.getUniformLocation("numberOfRows");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_ambient = super.getUniformLocation("ambient");

        location_useTexture = super.getUniformLocation("useTexture");
        location_useNormalMap = super.getUniformLocation("useNormalMap");
        location_useSpecularMap = super.getUniformLocation("useSpecularMap");
        location_useAnimation = super.getUniformLocation("isAnimated");

        location_textureMultiplier = super.getUniformLocation("tex_multiplier");
        location_normalMultiplier = super.getUniformLocation("norm_multiplier");
        location_specularMultiplier = super.getUniformLocation("spec_multiplier");

        location_jointTransforms = new int[MAX_JOINTS];
        for(int i=0; i<MAX_JOINTS; i++){
            location_jointTransforms[i] = super.getUniformLocation("jointTransforms[" + i + "]");
        }

        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_offset = super.getUniformLocation("offset");
    }

    // create samplers2D
    protected void connectTextureUnits(){
        super.loadInt(location_modelTexture, 0);
        super.loadInt(location_normalMap, 1);
        super.loadInt(location_specularMap, 2);
        //3 - tangents
        //4 - jointsIDs
        //5 - jointsWeights
        //6 - FREE
        super.loadInt(location_shadowMap, 7);
    }

    //protected void loadSkyColour(float r, float g, float b){
    //    super.loadVector3f(location_skyColour, new Vector3f(r,g,b));
    //}
    //protected void loadClipPlane(Vector4f plane){
    //    super.loadVector4f(location_plane, plane);
    //}

    protected void loadProjectionMatrix(Matrix4f projection){
        super.loadMatrix(location_projectionMatrix, projection);
    }

    protected void loadViewMatrix(Matrix4f viewMatrix){
        super.loadMatrix(location_viewMatrix, viewMatrix);
    }
    protected void loadLights(List<Light> lights, Matrix4f viewMatrix){
        for(int i=0;i<MAX_LIGHTS;i++){
            if(i<lights.size()){
                super.loadVector3f(location_lightPositionEyeSpace[i], getEyeSpacePosition(lights.get(i), viewMatrix));
                super.loadVector3f(location_lightColour[i], lights.get(i).getColor());
                super.loadVector3f(location_attenuation[i], lights.get(i).getAttenuation());
            }else{
                super.loadVector3f(location_lightPositionEyeSpace[i], new Vector3f(0, 0, 0));
                super.loadVector3f(location_lightColour[i], new Vector3f(0, 0, 0));
                super.loadVector3f(location_attenuation[i], new Vector3f(1, 0, 0));
            }
        }
    }
    protected void loadUseShadow(boolean use) {
        super.loadBoolean(location_useShadowMap, use);
    }

    protected void loadShadowVariables(Matrix4f toShadowSpaceMatrix, float shadowDistance, int multiSamplingQuality, int mapSize,
                                    float shadowTransitionWidth, float shadowTransitionOffset) {
        super.loadMatrix(location_toShadowMapSpace, toShadowSpaceMatrix);
        super.loadFloat(location_shadowDistance, shadowDistance);
        super.loadInt(location_shadowMultiSampling, multiSamplingQuality);
        super.loadInt(location_mapSize, mapSize);
        super.loadFloat(location_shadowTransitionWidth, shadowTransitionWidth);
        super.loadFloat(location_shadowTransitionOffset, shadowTransitionOffset);
    }

    protected void loadNumberOfRows(int numberOfRows){
        super.loadFloat(location_numberOfTextureRows, numberOfRows);
    }
    protected void loadShineVariables(float damper,float reflectivity, float ambient){
        super.loadFloat(location_shineDamper, damper);
        super.loadFloat(location_reflectivity, reflectivity);
        super.loadFloat(location_ambient, ambient);
    }


    protected void loadTextureBooleans(boolean useTexture, boolean useNormal, boolean useSpecular) {
        super.loadBoolean(location_useTexture, useTexture);
        super.loadBoolean(location_useNormalMap, useNormal);
        super.loadBoolean(location_useSpecularMap, useSpecular);
    }
    protected void loadAnimationBoolean(boolean useAnimation){
        super.loadBoolean(location_useAnimation, useAnimation);
    }

    protected void loadTexturesScales (float scaleTexture, float scaleNormal, float scaleSpecular) {
        super.loadFloat(location_textureMultiplier, scaleTexture);
        super.loadFloat(location_normalMultiplier, scaleNormal);
        super.loadFloat(location_specularMultiplier, scaleSpecular);
    }

    protected void loadJointTransforms(Matrix4f[] transforms){
        for(int i=0;i<transforms.length;i++){
            super.loadMatrix(location_jointTransforms[i], transforms[i]);
        }
    }

    protected void loadTransformationMatrix(Matrix4f matrix){
        super.loadMatrix(location_transformationMatrix, matrix);
    }
    protected void loadOffset(float x, float y){
        super.loadVector2f(location_offset, new Vector2f(x,y));
    }



    private Vector3f getEyeSpacePosition(Light light, Matrix4f viewMatrix){
        Vector3f position = light.getPosition();
        Vector4f eyeSpacePos = new Vector4f(position.x,position.y, position.z, 1f);
        Matrix4f.transform(viewMatrix, eyeSpacePos, eyeSpacePos);
        return new Vector3f(eyeSpacePos);
    }

}