package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;
import peersim.core.CommonState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by julian on 11/05/15.
 */
public class AvgShortestPathProgram implements ObserverProgram {


    int lastSize = 0;
    int tick = 0;

    public void tick(long currentTick, DictGraph observer) {

        if (this.lastSize < observer.size()) {
            this.tick += 1;
            if (this.tick >= 20) {
                this.tick = 0;
                this.lastSize = observer.size();
                final List<Long> ids = new ArrayList<Long>(observer.nodes.keySet());

                double total = 0.0;
                total += observer.avgReachablePaths(ids.get(CommonState.r.nextInt(ids.size()))).avg;

                total += observer.avgReachablePaths(ids.get(CommonState.r.nextInt(ids.size()))).avg;

                total += observer.avgReachablePaths(ids.get(CommonState.r.nextInt(ids.size()))).avg;

                System.out.println(observer.size() + " " + total/3);


            }
        }


    }

    public void onLastTick(DictGraph observer) {

    }

}
