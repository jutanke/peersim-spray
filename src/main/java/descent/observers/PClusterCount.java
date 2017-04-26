package descent.observers;

/**
 * Created by julian on 4/30/15.
 */
public class PClusterCount implements ObserverProgram {

    int prevSize = -1;
    final int COUNTDOWN = 30;
    int countDown = COUNTDOWN;

    /**
     *
     * @param currentTick
     * @param observer
     */
    public void tick(long currentTick, DictGraph observer) {

        if (prevSize == -1) {
            prevSize = observer.size();
        } else if (prevSize < observer.size()) {
            prevSize = observer.size();
        }

        if (prevSize > observer.size()) {
            // we are loosing nodes

            if (countDown == 0) {
                countDown = COUNTDOWN;
                prevSize = observer.size();
                DictGraph.ClusterResult r = observer.countClusters();
                System.out.println(r.count + " " + r.deadLinks + " " + r.maxClusterSize);
            }
            countDown--;
        }
    }

    /**
     *
     * @param observer
     */
    public void onLastTick(DictGraph observer) {

    }
}
