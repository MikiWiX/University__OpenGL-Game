package renderEngine;

import java.util.ArrayList;
import java.util.List;

public class MainThreadAction {

    /**
     * single missed handler
     * room 0 : loop initialization complete
     * room 2 : loop exit
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
