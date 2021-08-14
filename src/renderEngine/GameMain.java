package renderEngine;

import Input.Input;
import Input.Settings;
import org.lwjgl.glfw.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class GameMain {

    // We need to strongly reference callback instances.
    private static GLFWErrorCallback errorCallback;
    private static GLFWKeyCallback   keyCallback;
    private static GLFWWindowSizeCallback wsCallback;
    private static GLFWWindowCloseCallback clCallback;
    private static Callback debugProc;

    // The window handle
    private static long window;
    private static int width, height;
    private static float ratio;

    private static int IN_WIDTH, IN_HEIGHT;
    private static boolean RESET_FBO = false;
    private static boolean NOT_YET = false;

    public static void run() {
        try {
            MainThreadAction.createSignals(1);
            init();
            //run openGL in new thread
            glfwMakeContextCurrent(MemoryUtil.NULL);
            Thread openGLLoop = new Thread(new RenderLoopGLThread(debugProc, window));
            openGLLoop.start();
            MainThreadAction.doWait1(0, 0);

            //loop();
            while(RenderLoopGLThread.RENDER_ON) {
                glfwPollEvents();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            glfwMakeContextCurrent(window);
            // Release window and window callbacks
            glfwDestroyWindow(window);
            keyCallback.free();
            wsCallback.free();
            if (debugProc != null)
                debugProc.free();
        } finally {
            // Terminate GLFW and release the GLFWerrorfun
            glfwTerminate();
            errorCallback.free();
        }
    }

    private static void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 1);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1); //more detailed info about openGL bugs

        height = Settings.INIT_HEIGHT;
        width = Settings.INIT_WIDTH;
        ratio = (float)width / (float)height;
        IN_HEIGHT = Settings.INIT_IN_HEIGHT;
        IN_WIDTH = (int)(IN_HEIGHT*ratio);


        // Create the window
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        Input.setInput(window);
        keyCallback = Input.getKeyCallback();

        glfwSetWindowSizeCallback(window, wsCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                    ratio = (float)w / (float)h;
                    //screen ratio between 1:1 and 3:1
                    //render height doesn't change
                    float minRatio = Settings.MIN_RATIO;
                    float maxRatio = Settings.MAX_RATIO;
                    if (ratio < minRatio) {
                        glfwSetWindowSize(window, (int)(minRatio*h), h);
                        IN_WIDTH = (int)(IN_HEIGHT*minRatio);
                    } else if (ratio > maxRatio) {
                        glfwSetWindowSize(window, (int)(maxRatio*h), h);
                        IN_WIDTH = (int)(IN_HEIGHT*maxRatio);
                    } else {
                        IN_WIDTH = (int)(IN_HEIGHT*ratio);
                    }
                    RESET_FBO = true;
                    NOT_YET = true;
                }
            }

        });

        glfwSetWindowCloseCallback(window, clCallback = new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering renderLoop
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert vidmode != null : "vidmode of glfwGetVideoMode of initComponents function of renderEngine.GameMain is null.";
        glfwSetWindowPos(window, (vidmode.width() - width)/2, ((vidmode.height() - height)/2)+100);
        try (MemoryStack frame = MemoryStack.stackPush()) {
            IntBuffer framebufferSize = frame.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            width = framebufferSize.get(0);
            height = framebufferSize.get(1);
        }
        // Make the window visible
        glfwShowWindow(window);
        // Make the OpenGL context current thread
        glfwMakeContextCurrent(window);
        //glfwMakeContextCurrent(MemoryUtil.NULL); //<- opposite
        // Enable v-sync
        glfwSwapInterval(1);
        //GL.createCapabilities();
        //debugProc = GLUtil.setupDebugMessageCallback();
    }

    public static void main(String[] args) {
        new GameMain();
        GameMain.run();
    }

    private static long lastFrameTime;
    private static float deltaFrameTime;
    private static long deltaFrameTimeInMilis;


    public static void setLastFrameTime() {
        lastFrameTime = System.currentTimeMillis();
    }

    public static void setFrameRenderTime() {
        deltaFrameTimeInMilis = System.currentTimeMillis() - lastFrameTime;
        deltaFrameTime = (float) deltaFrameTimeInMilis / 1000;
    }

    //return seconds
    public static float getFrameRenderTime() {
        return deltaFrameTime;
    }

    public static long getDeltaFrameTimeInMilis() {
        return deltaFrameTimeInMilis;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getInWidth() {
        return IN_WIDTH;
    }

    public static int getInHeight() {
        return IN_HEIGHT;
    }

    public static float getRatio() {
        return ratio;
    }

    public static long getWindow() {
        return window;
    }

    public static boolean getResetFbo() {
        return RESET_FBO;
    }

    public static void zeroResetFbo() {
        RESET_FBO = false;
    }

    public static boolean getNotYetReset() {
        return NOT_YET;
    }

    public static void falseNotYetReset() {
        NOT_YET = false;
    }
}