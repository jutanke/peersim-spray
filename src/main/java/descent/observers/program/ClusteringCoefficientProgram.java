package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;

/**
 * Created by julian on 15/05/15.
 */
public class ClusteringCoefficientProgram implements ObserverProgram {

    public void tick(long currentTick, DictGraph observer) {
        System.out.println(observer.meanPartialViewSize() + " " + observer.meanClusterCoefficient());
    }

    public void onLastTick(DictGraph observer) {

    }
}
