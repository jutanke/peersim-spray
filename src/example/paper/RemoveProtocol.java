package example.paper;

import example.paper.scamplon.FastChurn;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Node;

/**
 * Created by julian on 3/31/15.
 */
public class RemoveProtocol implements Control {

    private static final String PAR_REM_START = "startRem";
    private static final String PAR_REM_END = "endRem";
    private static final String PARR_REM_COUNT = "removingPerStep";

    public static RemoveProtocol instance = null;

    public final long REMOVING_START;
    public final long REMOVING_END;
    public final int REMOVING_COUNT;

    public RemoveProtocol(String n) {
        instance = this;
        this.REMOVING_COUNT = Configuration.getInt(n + "." + PARR_REM_COUNT, 0);
        this.REMOVING_START = Configuration.getInt(n + "." + PAR_REM_START, Integer.MAX_VALUE);
        this.REMOVING_END = Configuration.getInt(n + "." + PAR_REM_END, Integer.MAX_VALUE);
    }


    @Override
    public boolean execute() {

        final long currentTimestamp = CommonState.getTime();
        final ChurnProtocol churn = ChurnProtocol.current;

        if (currentTimestamp >= this.REMOVING_START && currentTimestamp <= this.REMOVING_END) {
            // REMOVE ELEMENTS
            for (int i = 0; i < this.REMOVING_COUNT && churn.graph.size() > 0; i++) {
                final int pos = CommonState.r.nextInt(churn.graph.size());
                final Node rem = churn.graph.get(pos);
                churn.removeNode(rem);
                Dynamic d = (Dynamic) rem.getProtocol(churn.pid);
                if (d.isUp()) {
                    d.down();
                }
                churn.graph.remove(pos);
                churn.availableNodes.push(rem);
            }
        }

        return false;
    }
}
