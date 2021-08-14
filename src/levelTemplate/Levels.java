package levelTemplate;

import levelTemplate.custom.ComponentAnimator;
import levelTemplate.custom.ComponentLoader;
import levelTemplate.custom.ComponentLoadingScreen;
import levelTemplate.level1.Level1Main;

public class Levels {

    public static void playLevel1() {
        Level1Main main = new Level1Main();
        //main.play(ComponentLoader.class, ComponentAnimator.class, ComponentLoadingScreen.class);
        main.play();
    }
}
