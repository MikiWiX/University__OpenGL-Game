package levelComponents.texts;

import levelComponents.HasText;
import levelComponents.LevelComponent;
import levelComponents.ships.TestShip;
import levelTemplate.LevelLoader;
import levelTemplate.LevelMain;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LoadedDataBuffer;
import renderEngine.entities.Entity;
import renderEngine.entities.cameras.Camera;
import renderEngine.storage.Loader;
import renderEngine.text.fontMeshCreator.FontType;
import renderEngine.text.fontMeshCreator.GUIText;
import renderEngine.text.fontRendering.TextMaster;
import renderEngine.toolbox.org.lwjgl.util.vector.Vector2f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static levelTemplate.LoadedDataBuffer.Type.FONT;

public class TestText extends LevelComponent implements HasText {

    private TextMaster textRenderer;

    private List<FontType> fonts = new ArrayList<>();

    private List<Integer> fontPics = new ArrayList<>();
    private List<File> fontFiles = new ArrayList<>();
    private List<Integer> fontPaddings = new ArrayList<>();

    private Camera camera;
    private TestShip testShip;

    public TestText(ComponentMain levMain, List<LevelComponent> cmpList, TextMaster textRenderer, Camera camera, TestShip testShip){
        super(levMain, cmpList);
        this.textRenderer = textRenderer;
        this.camera = camera;
        this.testShip = testShip;
    }

    @Override
    protected void loadFiles() {
        LoadedDataBuffer font0 = LevelLoader.loadFontFiles("font/candara", "res/font/candara.fnt");
        font0.fontPadding = 6;
        font0.type = FONT;
        put(font0);
    }

    @Override
    protected void loadToOpenGL(Loader loader, LoadedDataBuffer pm) {
        int fontID = LevelMain.loadAsImage(loader, pm);
        fonts.add(new FontType(fontID,
                pm.fontInfoFile,
                pm.fontPadding));
        fontPics.add(fontID);
        fontFiles.add(pm.fontInfoFile);
        fontPaddings.add(pm.fontPadding);
    }

    @Override
    public void init() {}

    @Override
    public void update() {}

    @Override
    public void processDynamicTexts(List<GUIText> tList) {
        List<Float> textSet = asList(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        GUIText txt1 = new GUIText(String.valueOf(textSet), textRenderer, 1, fonts.get(0), new Vector2f(0.25f, 0.1f), 0.5f, true);
        tList.add(txt1);

        List<Entity> ships = testShip.getEntityList();
        if(ships != null && !ships.isEmpty()){
            textSet = ships.get(0).getPosition().getAsList();
            GUIText txt2 = new GUIText(String.valueOf(textSet), textRenderer, 1, fonts.get(0), new Vector2f(0.25f, 0.9f), 0.5f, true);
            tList.add(txt2);
        }

        GUIText txt3 = new GUIText("Lives: "+testShip.getLives(), textRenderer, 2, fonts.get(0), new Vector2f(0.04f, 0.04f), 1, false);
        txt3.setCharacterColor(0.6f,0.3f,0.3f);
        tList.add(txt3);

        if(testShip.getLives() <= 0){
            GUIText txt4 = new GUIText("Game Over", textRenderer, 5, fonts.get(0), new Vector2f(0.25f, 0.4f), 0.5f, true);
            txt4.setCharacterColor(0.1f,0,0.2f);
            txt4.setBorderWidth(0.6f);
            txt4.setBorderEdge(0.1f);
            txt4.setBorderColor(1,1,1);
            tList.add(txt4);
        }
    }

    @Override
    public void reloadFonts() {
        for (int i = 0; i<fontFiles.size(); i++) {
            fonts.set(i, new FontType(fontPics.get(i), fontFiles.get(i), fontPaddings.get(i)));
        }
    }
}
