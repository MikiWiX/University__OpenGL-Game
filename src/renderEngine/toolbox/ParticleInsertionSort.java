package renderEngine.toolbox;

import renderEngine.particles.Particle;

import java.util.List;

/**
 * A simple implementation of an insertion sort. I implemented this very quickly
 * the other day so it may not be perfect or the most efficient! Feel free to
 * implement your own sorter instead.
 *
 * @author Karl
 *
 */
public class ParticleInsertionSort {

    /**
     * Sorts a list of renderEngine.particles so that the renderEngine.particles with the highest distance
     * from the camera are first, and the renderEngine.particles with the shortest distance
     * are last.
     *
     * @param list
     *            - the list of renderEngine.particles needing sorting.
     */
    public static void sortHighToLow(List<Particle> list) {
        for (int i = 1; i < list.size(); i++) {
            Particle item = list.get(i);
            if (item.getCameraDistance() > list.get(i - 1).getCameraDistance()) {
                sortUpHighToLow(list, i);
            }
        }
    }

    private static void sortUpHighToLow(List<Particle> list, int i) {
        Particle item = list.get(i);
        int attemptPos = i - 1;
        while (attemptPos != 0 && list.get(attemptPos - 1).getCameraDistance() < item.getCameraDistance()) {
            attemptPos--;
        }
        list.remove(i);
        list.add(attemptPos, item);
    }

}