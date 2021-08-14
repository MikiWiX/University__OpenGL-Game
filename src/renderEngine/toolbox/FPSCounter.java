package renderEngine.toolbox;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class FPSCounter {

    private static List<Integer> lastSecondFrameTimes = new ArrayList<Integer>();
    private static long lastFrameTime = currentTimeMillis();

    public static int getFPSCount() {
        return lastSecondFrameTimes.size();
    }

    public static void addFrameAndRefreshCount() {
        long t = currentTimeMillis();
        long deltaFrameTime = t - lastFrameTime;
        lastFrameTime = t;
        lastSecondFrameTimes.add((int) deltaFrameTime);
        while(lastSecondFrameTimes.stream().mapToInt(Integer::intValue).sum()>1000) {
            lastSecondFrameTimes.remove(0);
        }
    }
}
