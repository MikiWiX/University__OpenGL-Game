package renderEngine.renderer;

import org.lwjgl.BufferUtils;
import renderEngine.toolbox.org.lwjgl.util.vector.Matrix4f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector3f;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector4f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public abstract class ShaderProgram {

    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    //for loading matrix4f
    private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram(String vertexFile, String fragmentFile) {
        //load, link and start using renderEngine.shaders
        vertexShaderID = loadShader(vertexFile, GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL_FRAGMENT_SHADER);

        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        glLinkProgram(programID);
        glValidateProgram(programID);
        getAllUniformLocation();
    }

    /*
    Uniforms are for example: camera rotation or object transformation matrix, or texture sampler
    uniformlocation is like name of an uniform in renderEngine.shaders
     */
    protected int getUniformLocation(String uniformName) {
        //get location of all uniform variables from java in the shader code
        return glGetUniformLocation(programID, uniformName);
    }

    protected abstract void getAllUniformLocation();

    /*
    now uniform loaders (to load uniforms from variablesz to renderEngine.shaders)
     */

    protected void loadInt(int location, int value) {
        glUniform1i(location, value);
    }
    protected void loadFloat(int location, float value) {
        glUniform1f(location, value);
    }
    protected void loadVector2f(int location, Vector2f vector) {
        glUniform2f(location, vector.x, vector.y);
    }
    protected void loadVector3f(int location, Vector3f vector) {
        glUniform3f(location, vector.x, vector.y, vector.z);
    }
    protected void loadVector4f(int location, Vector4f vector) {
        glUniform4f(location, vector.z, vector.y, vector.z, vector.w);
    }
    protected void loadBoolean(int location, boolean value) {
        float toLoad = 0;
        if (value) {
            toLoad = 1;
        }
        glUniform1f(location, toLoad);
    }
    protected void loadMatrix(int location, Matrix4f matrix) {
        matrix.store(matrixBuffer);
        matrixBuffer.flip();
        //loader, czy ma być transponowana = false
        glUniformMatrix4fv(location, false, matrixBuffer);
    }

    /*
    end of uniform handlers
     */

    public void start() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void cleanUP() {
        stop();
        glDetachShader(programID, fragmentShaderID);
        glDetachShader(programID, vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteProgram(programID);
    }

    protected void bindAttribute(int attribute, String variableName) {
        glBindAttribLocation(programID, attribute, variableName);
    }

    protected abstract void bindAttributes();

    private static int loadShader(String file, int type) {

        StringBuilder shaderSource = new StringBuilder();
        //reading from txt file to buffer and loading to Java variable
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch(IOException e) {
            System.err.println("Could not read file!");
            e.printStackTrace();
            System.exit(-1);
        }

        // loading from variable to OpenGL
        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if(glGetShaderi(shaderID, GL_COMPILE_STATUS)==GL_FALSE) {
            System.out.println(glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader.");
            System.exit(-1);
        }

        return shaderID;
    }
}
