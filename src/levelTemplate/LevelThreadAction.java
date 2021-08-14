package levelTemplate;

import renderEngine.ThreadWaitingRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class LevelThreadAction {

    /**
     * MUTEX
     */
    public static final ReentrantLock loadMutex = new ReentrantLock();
    public static final ReentrantLock guiMutex = new ReentrantLock();
    public static final ReentrantLock particleMutex = new ReentrantLock();
    public static final ReentrantLock entityMutex = new ReentrantLock();

    /**
     * single missed signal handler
     *
     * booleanFlag INIT_LOADING_DONE : loader -> main : loading finished, terminate loader
     * signal 0 : main -> animator : animator create entities
     * booleanFlag INIT_ANIMATOR_DONE : animator -> main : entities created
     * signal 1 : animator -> main : level animation finished, terminate animator
     */
    private static List<ThreadWaitingRoom> twr = new ArrayList<>();
    private static List<Boolean> wasSignalled = new ArrayList<>();

    public static void createSignals(int amount) {
        for (int i=0; i<amount; i++) {
            twr.add(new ThreadWaitingRoom());
            wasSignalled.add(false);
        }
    }

    public static void doWait1(int id, int time){
        synchronized(twr.get(id)){
            if(!wasSignalled.get(id)){
                try{
                    if (time > 0) {
                        twr.get(id).wait(time);
                    } else {
                        twr.get(id).wait();
                    }
                } catch(InterruptedException e){System.out.println("waitingThread 1 Error");}
            }
            //clear signal and continue running.
            wasSignalled.set(id, false);
        }
    }

    public static void doNotify1(int id){
        synchronized(twr.get(id)){
            wasSignalled.set(id, true);
            twr.get(id).notify();
        }
    }

    public static void cleanUP() {
        twr = null;
        twr = new ArrayList<>();
        wasSignalled = null;
        wasSignalled = new ArrayList<>();
    }
}
