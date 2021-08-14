package levelComponents;

import renderEngine.text.fontMeshCreator.GUIText;

import java.util.List;

public interface HasText {

    void processDynamicTexts(List<GUIText> tList);
    void reloadFonts();

}
