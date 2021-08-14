package renderEngine.toolbox;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class APSCounter {

    private static List<Integer> lastSecondAnimateTimes = new ArrayList<Integer>();
    private static long lastAnimateTime = currentTimeMillis();

    public static int getAPSCount() {
        return lastSecondAnimateTimes.size();
    }

    public static void addFrameAndRefreshCount() {
        long t = currentTimeMillis();
        long deltaFrameTime = t - lastAnimateTime;
        lastAnimateTime = t;
        lastSecondAnimateTimes.add((int) deltaFrameTime);
        while(lastSecondAnimateTimes.stream().mapToInt(Integer::intValue).sum()>1000) {
            lastSecondAnimateTimes.remove(0);
        }
    }
}
