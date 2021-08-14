package renderEngine;

import levelTemplate.Levels;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;
import renderEngine.entities.cameras.SimpleCam;
import renderEngine.loaders.png.de.mathiasmann.twl.PNGHandler;
import renderEngine.storage.RawPNGTexture;
import renderEngine.text.fontMeshCreator.FontType;

import java.io.File;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class RenderLoopGLThread implements Runnable {

    private static Callback debugProc;
    private static long window;
    protected static boolean RENDER_ON = true;

    public RenderLoopGLThread(Callback debugProc, long window) {
        RenderLoopGLThread.debugProc = debugProc;
        RenderLoopGLThread.window = window;
    }

    @Override
    public void run() {
        MainThreadAction.doNotify1(0);
        glfwMakeContextCurrent(window);
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();

        //glfwMakeContextCurrent(MemoryUtil.NULL);
        //Thread loading = new Thread(new LoadingScreenThread(window));
        //loading.start();
        //loadingPause();

        // Run the rendering renderLoop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        loadFPSFont();

        //while ( !glfwWindowShouldClose(window) ) {

//            SampleLevelMain main = new SampleLevelMain();
//            main.play(SampleLevelLoader.class, SampleLevelAnimator.class, BasicLoadingScreen.class, SimpleCam.class);
        Levels.playLevel1();

        //}

        cleanUP();

        //loadingCleanUP();
        glfwMakeContextCurrent(MemoryUtil.NULL);
        RENDER_ON = false;
    }

    public static FontType FPS_FONT;
    private static int fontImageID;
    private static File fontFile;
    private static void loadFPSFont() {

        RawPNGTexture fontImage = PNGHandler.decodeTextureFile("res/"+"font/candara"+".png");
        fontFile = new File("res/font/candara.fnt");

        fontImageID = GL11.glGenTextures();
        GL13.glActiveTexture(GL_TEXTURE0);
        GL11.glBindTexture(GL_TEXTURE_2D, fontImageID);
        GL11.glPixelStorei(GL_UNPACK_ALIGNMENT , 1);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, fontImage.getWidth(), fontImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, fontImage.getImage());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        GL11.glBindTexture(GL_TEXTURE_2D, 0);

        reloadFPSFont();
    }
    public static void reloadFPSFont() {
        FPS_FONT = new FontType(fontImageID, fontFile, 6);
    }
    private static void cleanUP() {
        FPS_FONT.cleanUP();
    }
}
