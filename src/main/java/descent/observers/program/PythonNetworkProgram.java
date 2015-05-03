package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;
import peersim.core.CommonState;

/**
 * This Program will print the network as Python-graph
 *
 * Created by julian on 02/05/15.
 */
public class PythonNetworkProgram implements ObserverProgram{

    final int step = 1;
    final int MAX_SIZE = 10000;
    boolean isFirst = true;

    /**
     *
     * @param currentTick
     * @param observer
     */
    public void tick(long currentTick, DictGraph observer) {

        if (CommonState.getTime() > 140 && CommonState.getTime() % step == 0) {
            boolean imp = this.isFirst;
            this.isFirst =false;
            System.out.println("#=================START=================== step:" + CommonState.getTime());
            System.out.println(observer.networkxDigraph(DictGraph.NetworkX.Connectedness, "g" + (MAX_SIZE - observer.size()), imp));
            System.out.println("#=================END===================");
        }

    }

    /**
     *
     * @param observer
     */
    public void onLastTick(DictGraph observer) {
    }
}
