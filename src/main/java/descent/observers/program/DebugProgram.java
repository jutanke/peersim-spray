package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.Observer;
import descent.observers.ObserverProgram;

/**
 * Created by julian on 4/29/15.
 */
public class DebugProgram implements ObserverProgram {

    public void tick(long currentTick, DictGraph observer) {
        System.out.println("qq");
        if (currentTick > 0) {
            System.out.println(observer.countArcs());
        }
    }

    public void onLastTick(DictGraph observer) {

    }
}
