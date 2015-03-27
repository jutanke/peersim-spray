package example.paper.cyclon;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.util.*;

/**
 * Created by julian on 3/27/15.
 */
public class Churn implements Control {

    private static final String PROTOCOL = "o1";
    private static final String PAR_ADD_COUNT = "adding";
    private static final String PARR_REM_COUNT = "removing";
    private static final String PAR_ADD_START = "startadd";
    private static final String PAR_REM_START = "startrem";
    private static final String PAR_ADD_END = "startadd";
    private static final String PAR_REM_END = "startrem";

    public final int ADDING_COUNT;
    public final int REMOVING_COUNT;
    public final long ADDING_START;
    public final long REMOVING_START;
    public final long REMOVING_END;
    public final long ADDING_END;

    List<Node> graph = new ArrayList<Node>();
    LinkedList<Integer> availableNodes = new LinkedList<Integer>();

    public Churn(String n) {
        this.ADDING_COUNT = Configuration.getInt(n + "." + PAR_ADD_COUNT, 0);
        this.REMOVING_COUNT = Configuration.getInt(n + "." + PARR_REM_COUNT, 0);
        this.ADDING_START = Configuration.getInt(n + "." + PAR_ADD_START, Integer.MAX_VALUE);
        this.REMOVING_START = Configuration.getInt(n + "." + PAR_REM_START, Integer.MAX_VALUE);
        this.REMOVING_END = Configuration.getInt(n + "." + PAR_ADD_END, Integer.MAX_VALUE);
        this.ADDING_END = Configuration.getInt(n + "." + PAR_REM_END, Integer.MAX_VALUE);
        final int nsize = Network.size();
        for (int i = 0; i < nsize; i++) {
            availableNodes.add(i);
        }
    }

    @Override
    public boolean execute() {

        final long currentTimestamp = CommonState.getTime();
        if (currentTimestamp >= this.ADDING_START && currentTimestamp <= this.ADDING_END) {
            // ADD ELEMENTS
            if (graph.size() == 0) {
                Node n0 = Network.get(this.availableNodes.poll());
                this.graph.add(n0);
            } else {

            }

        }

        if (currentTimestamp >= this.REMOVING_START && currentTimestamp <= this.REMOVING_END) {
            // REMOVE ELEMENTS

        }

        return false;
    }
}
