package Input;

import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

    private static GLFWKeyCallback keyCallback;

    private static boolean pauseResume = false;

    // camera motion/rotation stuff
    private static boolean moveCamFront = false;
    private static boolean moveCamBack = false;
    private static boolean moveCamLeft = false;
    private static boolean moveCamRight = false;
    private static boolean rotateCamUp = false;
    private static boolean rotateCamDown = false;
    private static boolean rotateCamLeft = false;
    private static boolean rotateCamRight = false;

    // ship control
    private static boolean moveShipRight = false;
    private static boolean moveShipLeft = false;
    private static boolean shoot = false;


    public static void setInput(long window) {
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if ( action == GLFW_PRESS ) {

                    // camera motion/rotation stuff
                    if ( key == GLFW_KEY_W ) moveCamFront = true;
                    if ( key == GLFW_KEY_S ) moveCamBack = true;
                    if ( key == GLFW_KEY_A ) moveCamLeft = true;
                    if ( key == GLFW_KEY_D ) moveCamRight = true;
                    if ( key == GLFW_KEY_UP ) rotateCamUp = true;
                    if ( key == GLFW_KEY_DOWN ) rotateCamDown = true;
                    if ( key == GLFW_KEY_LEFT ) rotateCamLeft = true;
                    if ( key == GLFW_KEY_RIGHT ) rotateCamRight = true;

                    // ship control
                    if ( key == GLFW_KEY_L ) moveShipRight = true;
                    if ( key == GLFW_KEY_J ) moveShipLeft = true;

                    if ( key == GLFW_KEY_SPACE ) shoot = true;

                }
                if (action == GLFW_RELEASE ) {
                    if ( key == GLFW_KEY_ESCAPE ) {
                        glfwSetWindowShouldClose(window, true); // We will detect this in our rendering renderLoop
                        //pauseResume = true;
                    }

                    // camera motion/rotation stuff
                    if ( key == GLFW_KEY_W ) moveCamFront = false;
                    if ( key == GLFW_KEY_S ) moveCamBack = false;
                    if ( key == GLFW_KEY_A ) moveCamLeft = false;
                    if ( key == GLFW_KEY_D ) moveCamRight = false;
                    if ( key == GLFW_KEY_UP ) rotateCamUp = false;
                    if ( key == GLFW_KEY_DOWN ) rotateCamDown = false;
                    if ( key == GLFW_KEY_LEFT ) rotateCamLeft = false;
                    if ( key == GLFW_KEY_RIGHT ) rotateCamRight = false;

                    // ship control
                    if ( key == GLFW_KEY_L ) moveShipRight = false;
                    if ( key == GLFW_KEY_J ) moveShipLeft = false;

                    if ( key == GLFW_KEY_SPACE ) shoot = false;
                }
            }
        });
    }

    public static GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    public static ArrayList<Boolean> getCameraMove() {
        return new ArrayList<Boolean>(List.of(moveCamFront, moveCamBack, moveCamLeft, moveCamRight, rotateCamUp, rotateCamDown, rotateCamLeft, rotateCamRight));
    }

    public static boolean getMoveShipRight() {
        return moveShipRight;
    }

    public static boolean getMoveShipLeft() {
        return moveShipLeft;
    }

    public static boolean getPressShoot(){
        boolean oldShoot = shoot;
        shoot = false;
        return oldShoot;
    }
    public static boolean getHoldShoot(){
        return shoot;
    }
}
