package renderEngine.storage;

import Input.Settings;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL15.*;


public class Loader {

    //we want to delete all VAOs and VBOs after game closing, thats why we create class variables
    //these lists will keep track of our containers created
    private List<Vao> vaos = new ArrayList<>();
    private List<Integer> textures = new ArrayList<>();

    public Model createModelFromOBJFile(RawOBJModel meshData) {
        Vao vao = Vao.create();
        vao.bind();
        //loads vertex to VBO 0, texture coordinates to VBO 1, and so on...
        vao.createAttribute(0, meshData.getVertices(), 3);
        vao.createAttribute(1, meshData.getTextureCoords(), 2);
        vao.createAttribute(2, meshData.getNormals(), 3);
        vao.createAttribute(3, meshData.getTangents(), 3);
        if (meshData.getIsAnimated()) {
            vao.createIntAttribute(4, meshData.getJointIds(), 3);
            vao.createAttribute(5, meshData.getVertexWeights(), 3);
        }
        //loads IBO
        vao.createIndexBuffer(meshData.getIndices());
        vao.unbind();

        vaos.add(vao);
        return new Model(vao);
    }

    /**
     * NEW !!!
     */
    public int loadTextureToOpenGL(RawPNGTexture texture, boolean[] textureSettings) {

        int texID = GL11.glGenTextures();
        GL13.glActiveTexture(GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, texID);
        GL11.glPixelStorei(GL_UNPACK_ALIGNMENT , 1);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, texture.getImage());

        if (textureSettings[0] && Settings.MIPMAPPING_ON) {
            GL30.glGenerateMipmap(GL_TEXTURE_2D);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

            if (textureSettings[1] && GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                GL11.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS , Settings.MIPMAPPING_BIAS_QUALITY);
                int filterQuality = Settings.ANISOTROPIC_FILTERING;
                if (filterQuality > 0) {
                    if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                        //quality of filtering compared with max supported amount
                        float amount = Math.min(filterQuality, glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
                        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
                    } else {
                        System.out.println("No support for Anisotropic Filtering");
                    }
                }
            }

        } else if (textureSettings[2]) {
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        } else {
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        }

        if (textureSettings[3]) {
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        } else {
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        }

        GL11.glBindTexture(GL_TEXTURE_2D, 0);

        textures.add(texID);
        return texID;
    }

    public Model createGuiModel(float[] positions) {
        Vao vao = Vao.create();
        vao.bind();
        vao.createAttribute(0, positions, 2);
        vao.unbind();
        vao.setVertexCount(positions.length/2);
        vaos.add(vao);
        return new Model(vao);
    }

    public int loadGuiImage(RawPNGTexture texture){

        int texID = GL11.glGenTextures();
        GL13.glActiveTexture(GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, texID);
        GL11.glPixelStorei(GL_UNPACK_ALIGNMENT , 1);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, texture.getImage());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        GL11.glBindTexture(GL_TEXTURE_2D, 0);

        textures.add(texID);
        return texID;
    }

    public Model createTextModel(float[] positions, float[] textureCords) {
        Vao vao = Vao.create();
        vao.bind();
        //loads vertex to VBO 0 and texture coordinates to VBO 1
        //argument 1: VBO number, then element size - textures are 2d while vertexes 3d, and lastly data
        vao.createAttribute(0, positions, 2);
        vao.createAttribute(1, textureCords, 2);
        vao.unbind();
        vaos.add(vao);
        return new Model(vao);
    }

    public void cleanUP() {
        for(Vao vao:vaos) {
            vao.delete();
        }
        for(int texture:textures) {
            glDeleteTextures(texture);
        }
    }
}