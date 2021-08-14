package levelTemplate;

import renderEngine.gui.GuiRenderer;
import renderEngine.gui.GuiTexture;
import renderEngine.storage.Loader;

import java.util.ArrayList;
import java.util.List;

public abstract class LoadingScreen {

    protected LevelMain<?,?,?> levMain;
    protected Loader loader;
    protected GuiRenderer guiRenderer;

    public LoadingScreen(LevelMain<?,?,?> levMain){
        this.levMain = levMain;
        this.loader = levMain.loader;
        this.guiRenderer = levMain.guiRenderer;
    }
    //gui list used on loading screen
    protected List<GuiTexture> loadingGuis = new ArrayList<>();

    /**
     * 6 loading screen handling functions
     */
    public abstract void loadLoadingGui();
    public abstract void resizeLoadingGui();
    public abstract void resetLoadingScreen();
    public abstract void renderLoadingScreen();
    public abstract void moveLoadingIcon();
    public abstract void renderLoadingIcon();

}
