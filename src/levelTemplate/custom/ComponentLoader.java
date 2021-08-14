package levelTemplate.custom;

import levelComponents.LevelComponent;
import levelTemplate.LevelLoader;

import java.util.List;

public class ComponentLoader extends LevelLoader<ComponentMain> {

    // -- variables shared with levelMain
    public List<LevelComponent> levelComponentsList;

    public <M extends ComponentMain> ComponentLoader(M levMain) {
        super(levMain);
    }


    @Override
    protected void loadFiles() {

        for(LevelComponent component : levMain.components){
            component.load();
        }

    }
}
