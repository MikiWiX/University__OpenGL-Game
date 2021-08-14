package renderEngine.particles;

public class ParticleTexture {

    private int textureID;
    // texture stages
    private int numberOfCols;
    // for texture randomization over one atlas
    private int numberOfRows;

    private boolean alphaBlending;

    public ParticleTexture(int textureID, int numberOfCols, int numberOfRows, boolean alphaBlending) {
        this.textureID = textureID;
        this.numberOfCols = numberOfCols;
        this.numberOfRows = numberOfRows;
        this.alphaBlending = alphaBlending;
    }

    public int getTextureID() {
        return textureID;
    }

    public int getNumberOfCols() {
        return numberOfCols;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public boolean getAlphaBlending() {
        return alphaBlending;
    }
}
