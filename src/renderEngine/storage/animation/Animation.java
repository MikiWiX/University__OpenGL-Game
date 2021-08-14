package renderEngine.storage.animation;

public class Animation {
    private final float length;
    private final KeyFrame[] keyFrames;

    public Animation(float lengthInSeconds, KeyFrame[] frames) {
        this.keyFrames = frames;
        this.length = lengthInSeconds;
    }

    public float getLength() {
        return this.length;
    }

    public KeyFrame[] getKeyFrames() {
        return this.keyFrames;
    }
}
