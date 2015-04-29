package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.Observer;
import descent.observers.ObserverProgram;

/**
 * Created by julian on 4/29/15.
 */
public class DebugProgram implements ObserverProgram {
    @Override
    public void tick(long currentTick, DictGraph observer) {

        if (currentTick > 0) {
            System.out.println(observer.countArcs());
        }
    }

    @Override
    public void onLastTick(DictGraph observer) {

    }
}
