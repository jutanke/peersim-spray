package example.paper;

import example.paper.cyclon.CyclonProtocol;
import example.paper.scamplon.PartialView;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.util.LinkedList;

/**
 * Created by julian on 3/28/15.
 */
public abstract class ChurnProtocol implements Control {

    public static ChurnProtocol current;

    private static final String PROTOCOL = "o1";
    private static final String PAR_ADD_COUNT = "addingPerStep";
    private static final String PARR_REM_COUNT = "removingPerStep";
    private static final String PAR_ADD_START = "startAdd";
    private static final String PAR_REM_START = "startRem";
    private static final String PAR_ADD_END = "endAdd";
    private static final String PAR_REM_END = "endRem";

    public final int ADDING_COUNT;
    public final int REMOVING_COUNT;
    public final long ADDING_START;
    public final long REMOVING_START;
    public final long REMOVING_END;
    public final long ADDING_END;
    public final int pid;

    public LinkedList<Node> graph = new LinkedList<Node>();
    public LinkedList<Node> availableNodes = new LinkedList<Node>();

    public ChurnProtocol(String n, String cyclProtocol) {
        this.ADDING_COUNT = Configuration.getInt(n + "." + PAR_ADD_COUNT, 0);
        this.REMOVING_COUNT = Configuration.getInt(n + "." + PARR_REM_COUNT, 0);
        this.ADDING_START = Configuration.getInt(n + "." + PAR_ADD_START, Integer.MAX_VALUE);
        this.REMOVING_START = Configuration.getInt(n + "." + PAR_REM_START, Integer.MAX_VALUE);
        this.REMOVING_END = Configuration.getInt(n + "." + PAR_REM_END, Integer.MAX_VALUE);
        this.ADDING_END = Configuration.getInt(n + "." + PAR_ADD_END, Integer.MAX_VALUE);
        final int nsize = Network.size();
        this.pid = Configuration.lookupPid(cyclProtocol);
        for (int i = 0; i < nsize; i++) {
            final Node node = Network.get(i);
            Dynamic d = (Dynamic) node.getProtocol(pid);
            d.down();
            availableNodes.add(node);
            System.err.println("Churn insert:" + this.ADDING_COUNT +
                    " [" + this.ADDING_START + ".." + this.ADDING_END + "]");
            System.err.println("Churn remove:" + this.REMOVING_COUNT +
                    " [" + this.REMOVING_START + ".." + this.REMOVING_END + "]");
        }
    }

    @Override
    public boolean execute() {

        final long currentTimestamp = CommonState.getTime();
        final boolean removingElements = currentTimestamp >= this.REMOVING_START && currentTimestamp <= this.REMOVING_END;
        final boolean addingElements = currentTimestamp >= this.ADDING_START && currentTimestamp <= this.ADDING_END;

        if (removingElements) {
            // REMOVE ELEMENTS
            for (int i = 0; i < this.REMOVING_COUNT && this.graph.size() > 0; i++) {
                final int pos = CommonState.r.nextInt(this.graph.size());
                final Node rem = this.graph.get(pos);
                this.removeNode(rem);
                Dynamic d = (Dynamic) rem.getProtocol(pid);
                if (d.isUp()) {
                    d.down();
                }
                this.graph.remove(pos);
                this.availableNodes.push(rem);
            }
        }

        if (addingElements) {
            // ADD ELEMENTS

            for (int i = 0; i < this.ADDING_COUNT && this.availableNodes.size() > 0; i++) {
                final Node current = this.availableNodes.poll();
                final Dynamic d = (Dynamic) current.getProtocol(pid);
                d.up();
                if (graph.size() > 0) {
                    //final Node contact = getBestNode();
                    final Node contact = getNode();
                    this.addNode(current, contact);
                }
                this.graph.add(current);
            }
        }

        return false;
    }

    public Node getNode() {
        return this.graph.get(CommonState.r.nextInt(this.graph.size()));
    }

    public Node getBestNode() {
        if (false) {
            return this.graph.get(CommonState.r.nextInt(this.graph.size()));
        }

        Node a = graph.get(CommonState.r.nextInt(this.graph.size()));
        Node b = graph.get(CommonState.r.nextInt(this.graph.size()));
        //Node c = graph.get(CommonState.r.nextInt(this.graph.size()));

        Dynamic A = (Dynamic) a.getProtocol(pid);
        Dynamic B = (Dynamic) b.getProtocol(pid);

        if (A.degree() > B.degree()) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * @param node
     * @return when true then select this node, otherwise do not select it
     */
    public abstract void removeNode(Node node);

    public abstract void addNode(Node subscriber, Node contact);
}
