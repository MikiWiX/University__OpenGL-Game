package levelTemplate.custom;

import levelTemplate.LevelMain;
import levelTemplate.LoadingScreen;
import levelTemplate.LevelThreadAction;
import renderEngine.GameMain;
import renderEngine.gui.GuiTexture;
import renderEngine.loaders.png.PNGLoader;
import renderEngine.storage.RawPNGTexture;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public class ComponentLoadingScreen extends LoadingScreen {


    public ComponentLoadingScreen(LevelMain<?,?,?> levMain) {
        super(levMain);
    }

    @Override
    public void loadLoadingGui() {
        float x = GameMain.getWidth();
        float y = GameMain.getHeight();
        RawPNGTexture icon = PNGLoader.loadPNGFile("res/loading/icon2.png");
        loadingGuis.add(new GuiTexture(loader.loadGuiImage(icon), new Vector2f(0.8f,-0.8f), new Vector2f(y*0.1f/x,0.1f), 0));
    }
    @Override
    public void resizeLoadingGui() {
        float x = GameMain.getWidth();
        float y = GameMain.getHeight();
        loadingGuis.get(0).resetGuiPlacement(new Vector2f(0.8f,-0.8f), new Vector2f(y*0.1f/x,0.1f));
    }
    @Override
    public void resetLoadingScreen() {
        glClearColor(0f,0f,0f, 0f);
        glClear(GL_COLOR_BUFFER_BIT);
        GameMain.setLastFrameTime();
        guiRenderer.render(loadingGuis);
        glfwSwapBuffers(levMain.window);
    }
    @Override
    public void renderLoadingScreen(){
        glClear(GL_COLOR_BUFFER_BIT);
        GameMain.setFrameRenderTime();
        GameMain.setLastFrameTime();
        loadingGuis.get(0).rotateGui(100);
        guiRenderer.render(loadingGuis);
        glfwSwapBuffers(levMain.window); // swap the color buffers
    }
    @Override
    public void moveLoadingIcon() {
        LevelThreadAction.guiMutex.lock();
        loadingGuis.get(0).rotateGui(100);
        LevelThreadAction.guiMutex.unlock();
    }
    @Override
    public void renderLoadingIcon() {
        LevelThreadAction.guiMutex.lock();
        guiRenderer.render(loadingGuis);
        LevelThreadAction.guiMutex.unlock();
    }
}
