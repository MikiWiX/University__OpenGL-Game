package Input;

public class Config {

    // set target loop time for animators. Too high frame rates will cause animator to lock mutexes more often
    // (and PC to compute a lot of unnecessary data), thus blocking access in rendering loop, decreasing FPS.
    // value in milliseconds, represents target time of one loop
    public static int ANIMATION_THREAD_LOOP_TARGET_TIME = 10;

    //currently used only by renderEngine.particles
    public static float GRAVITY = -10;

    // the more often you sort, the better result and worse performance
    public static float PARTICLES_SORT_INTERVAL = 0.1f;
    // dynamic buffer is technically a bit slower but won't overflow.
    public static boolean DYNAMIC_MAX_PARTICLES = true;
    // only if buffer not dynamic. Make sure number of rendered renderEngine.particles won't overflow it!
    public static int MAX_PARTICLES = 10000;

}
