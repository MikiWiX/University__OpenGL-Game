package levelComponents.gui;

import levelComponents.HasGui;
import levelComponents.LevelComponent;
import levelTemplate.LevelLoader;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LoadedDataBuffer;
import renderEngine.GameMain;
import renderEngine.gui.GuiTexture;
import renderEngine.storage.Loader;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static levelTemplate.LoadedDataBuffer.Type.GUI;


public class TestGui extends LevelComponent implements HasGui {

    public List<Integer> guiTextures = new ArrayList<>();
    protected List<GuiTexture> guis = new ArrayList<>();

    public TestGui(ComponentMain levMain, List<LevelComponent> cmpList){
        super(levMain, cmpList);
    }

    @Override
    protected void loadFiles() {
        LoadedDataBuffer gui0 = LevelLoader.loadImageFile("Gui/background");
        gui0.type = GUI;
        put(gui0);
    }

    @Override
    protected void loadToOpenGL(Loader loader, LoadedDataBuffer pm) {
        guiTextures.add(ComponentMain.loadAsImage(loader, pm));
    }

    @Override
    public void init() {
        setGui();
    }

    @Override
    public void update() {

    }

    @Override
    public void setGui() {
        float x = GameMain.getWidth();
        float y = GameMain.getHeight();
        GuiTexture gui0 = new GuiTexture(guiTextures.get(0), new Vector2f(-1f, 0f), new Vector2f(0.5f, 500f/y), 0);

        guis = asList(gui0);
    }

    @Override
    public List<GuiTexture> getGui() {
        return guis;
    }

    @Override
    public Stream<GuiTexture> getGuiStream() {
        return guis.stream();
    }
}
