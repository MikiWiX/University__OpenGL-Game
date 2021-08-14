package Input;

public class Settings {

    // initial window size
    public static int INIT_HEIGHT = 1200;
    public static int INIT_WIDTH = 1600;

    //initial render size
    public static int INIT_IN_HEIGHT = 600;
    // current mode: auto calculate width based on window ratio
    public static int INIT_IN_WIDTH = 1200;

    //max nad min screen ratio
    public static float MIN_RATIO = 1.3f;
    public static float MAX_RATIO = 2.5f;

    public static boolean ANTI_ALIAS_ON = true;
    // 1 = none, the more the better
    public static int ANTI_ALIAS_QUALITY = 4;

    // CHANGES TO MIPMAPPING NEED TO RELOAD (SCENE) TO APPLY
    public static boolean MIPMAPPING_ON = true;
    // the less the sharper [-1]
    public static float MIPMAPPING_BIAS_QUALITY = -1;
    // 0 = off, the more the better, positives only [4]
    public static int ANISOTROPIC_FILTERING = 0;

    // ---post processing---
    public static float BRIGHTNESS = 1f;
    public static float CONTRAST = 1f;


    // vary depends on monitor and window size [50]
    public static float FOV = 40;
    // [1000]
    public static float RENDER_MAX_DISTANCE = 4000f;
    // [0.1]
    public static float RENDER_MIN_DISTANCE = 1f;

    public static boolean SHADOWS_ON = true;
    // [2048]
    public static int SHADOW_RES = 2048;
    // [2] to [4]
    public static int SHADOW_MULTI_SAMPLING_QUALITY = 2;
    // [200]
    public static float SHADOW_DISTANCE = 1000;
    // just-in-case, you can mess up with renderEngine.shadows. unless its needed, leave default [150]
    public static float SHADOW_OFFSET = 150;
    // shader uniform, width of transition at which shadows disappear [10]
    public static float SHADOW_TRANSITION_WIDTH = 10;
    // uniform again, offset of transition [100]
    public static float SHADOW_TRANSITION_OFFSET = 100;

    public static boolean SHOW_FPS = true;

    //TODO
    public static int CONTROLS;
}
