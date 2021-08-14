package levelComponents;

import renderEngine.gui.GuiTexture;

import java.util.List;
import java.util.stream.Stream;

public interface HasGui {

    void setGui();
    List<GuiTexture> getGui();
    Stream<GuiTexture> getGuiStream();
}
