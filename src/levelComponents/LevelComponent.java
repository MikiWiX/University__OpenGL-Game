package levelComponents;

import levelTemplate.LoadingScreen;
import levelTemplate.custom.ComponentMain;
import levelTemplate.LevelThreadAction;
import levelTemplate.LoadedDataBuffer;
import renderEngine.storage.Loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class LevelComponent {

    private List<LoadedDataBuffer> preparedModels = new ArrayList<>();
    public boolean LOADING_DONE = false;

    public HashMap<Class<? extends LevelComponent>, List<LevelComponent>> components = new HashMap<>();

    public ComponentMain levMain;

    public LevelComponent(ComponentMain levMain, List<LevelComponent> cmpList){
        cmpList.add(this);
        this.levMain = levMain;
    }

    @SafeVarargs
    public final <C extends LevelComponent> void addComponent(boolean reverseBind, C... components){
        for(C component : components){
            //add component here
            List<LevelComponent> list = this.components.computeIfAbsent(component.getClass(), k -> new ArrayList<>());
            list.add(component);

            if (reverseBind) {
                //add this to the component
                List<LevelComponent> secondComponentList = component.components.computeIfAbsent(this.getClass(), k -> new ArrayList<>());
                if( !secondComponentList.contains(this) ){
                    secondComponentList.add(this);
                }
            }
        }
    }

    protected abstract void loadFiles();
    protected abstract void loadToOpenGL(Loader loader, LoadedDataBuffer pm);
    public abstract void init();
    public abstract void update();

    public final void load(){
        loadFiles();
        LOADING_DONE = true;
    }
    public final void processToOpenGlLoop(Loader loader, LoadingScreen loadingScreen){
        while(!LOADING_DONE){
            LevelThreadAction.loadMutex.lock();
            if(!preparedModels.isEmpty()){
                for (LoadedDataBuffer pm : preparedModels) {
                    loadToOpenGL(loader, pm);
                }
                preparedModels.clear();
            }
            LevelThreadAction.loadMutex.unlock();

            //render screen
            loadingScreen.renderLoadingScreen();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LevelThreadAction.loadMutex.lock();
        if(!preparedModels.isEmpty()){
            for (LoadedDataBuffer pm : preparedModels) {
                loadToOpenGL(loader, pm);
            }
            preparedModels.clear();
        }
        LevelThreadAction.loadMutex.unlock();

        loadingScreen.renderLoadingScreen();
    }

    protected final void put(LoadedDataBuffer entrance) {
        LevelThreadAction.loadMutex.lock();
        preparedModels.add(entrance);
        LevelThreadAction.loadMutex.unlock();
    }
    protected final void put(List<LoadedDataBuffer> list) {
        LevelThreadAction.loadMutex.lock();
        preparedModels.addAll(list);
        LevelThreadAction.loadMutex.unlock();
    }
    protected final void put(LoadedDataBuffer[] array) {
        LevelThreadAction.loadMutex.lock();
        preparedModels.addAll(Arrays.asList(array));
        LevelThreadAction.loadMutex.unlock();
    }

}
