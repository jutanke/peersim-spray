package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;

/**
 *
 */
public class ArcCountProgram implements ObserverProgram {

    /**
     *
     * @param currentTick {}
     * @param observer {}
     */
    public void tick(long currentTick, DictGraph observer) {
        System.out.println(observer.countArcs() + " " + observer.size());
    }

    /**
     *
     * @param observer {}
     */
    public void onLastTick(DictGraph observer) {

    }
}
