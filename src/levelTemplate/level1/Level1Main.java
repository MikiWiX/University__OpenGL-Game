package levelTemplate.level1;

import levelComponents.alienWeapons.TestProjectile;
import levelComponents.aliens.TestAliens;
import levelComponents.gui.TestGui;
import levelComponents.ships.TestShip;
import levelComponents.terrain.regular.Desert;
import levelComponents.texts.TestText;
import levelComponents.weapons.TestRocket;
import levelTemplate.custom.ComponentMain;

public class Level1Main extends ComponentMain {

    @Override
    public void addComponents() {

        Desert desert = new Desert(this, components);
        TestShip testShip = new TestShip(this, components, new TestRocket(this, components));
        TestAliens testAliens = new TestAliens(this, components, testShip, new TestProjectile(this, components, testShip));
        TestRocket testRocket = new TestRocket(this, components);
        TestGui testGui = new TestGui(this, components);
        TestText testText = new TestText(this, components, textRenderer, camera, testShip);

        desert.addComponent(false, testAliens);
    }
}
