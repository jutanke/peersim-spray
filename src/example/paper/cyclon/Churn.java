package example.paper.cyclon;

import example.paper.Dynamic;
import peersim.config.Configuration;
import peersim.core.*;

import java.util.*;

/**
 * Created by julian on 3/27/15.
 */
public class Churn implements Control {

    private static final String PROTOCOL = "o1";
    private static final String PAR_ADD_COUNT = "addingPerStep";
    private static final String PARR_REM_COUNT = "removing";
    private static final String PAR_ADD_START = "startAdd";
    private static final String PAR_REM_START = "startrem";
    private static final String PAR_ADD_END = "startadd";
    private static final String PAR_REM_END = "startrem";

    public final int ADDING_COUNT;
    public final int REMOVING_COUNT;
    public final long ADDING_START;
    public final long REMOVING_START;
    public final long REMOVING_END;
    public final long ADDING_END;
    private final int pid;

    LinkedList<Node> graph = new LinkedList<Node>();
    LinkedList<Node> availableNodes = new LinkedList<Node>();

    public Churn(String n) {
        this.ADDING_COUNT = Configuration.getInt(n + "." + PAR_ADD_COUNT, 0);
        this.REMOVING_COUNT = Configuration.getInt(n + "." + PARR_REM_COUNT, 0);
        this.ADDING_START = Configuration.getInt(n + "." + PAR_ADD_START, Integer.MAX_VALUE);
        this.REMOVING_START = Configuration.getInt(n + "." + PAR_REM_START, Integer.MAX_VALUE);
        this.REMOVING_END = Configuration.getInt(n + "." + PAR_ADD_END, Integer.MAX_VALUE);
        this.ADDING_END = Configuration.getInt(n + "." + PAR_REM_END, Integer.MAX_VALUE);
        final int nsize = Network.size();
        this.pid = Configuration.lookupPid(CyclonProtocol.PAR_PROT);
        for (int i = 0; i < nsize; i++) {
            final Node node = Network.get(i);
            Dynamic d = (Dynamic) node.getProtocol(pid);
            d.down();
            availableNodes.add(node);
        }
    }

    @Override
    public boolean execute() {

        final long currentTimestamp = CommonState.getTime();
        if (currentTimestamp >= this.ADDING_START && currentTimestamp <= this.ADDING_END) {
            // ADD ELEMENTS

            for (int i = 0; i < this.ADDING_COUNT && this.availableNodes.size() > 0; i++) {
                final Node current = this.availableNodes.poll();
                final Dynamic d = (Dynamic) current.getProtocol(pid);
                d.up();
                if (graph.size() > 0) {
                    final int pos = CommonState.r.nextInt(this.graph.size());
                    final Node contact = graph.get(pos); // INDIRECTION
                    CyclonProtocol pp = (CyclonProtocol) current.getProtocol(this.pid);
                    pp.addNeighbor(contact);
                    System.err.println("add " + current.getID() + " -> " + contact.getID() + " s:" + current.isUp());
                }
                this.graph.add(current);
            }
        }

        if (currentTimestamp >= this.REMOVING_START && currentTimestamp <= this.REMOVING_END) {
            // REMOVE ELEMENTS

        }

        return false;
    }
}
