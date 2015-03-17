package example.paper.scamp;

import peersim.cdsim.CDProtocol;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.List;

/**
 * Created by julian on 3/14/15.
 */
public class Scamp implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    // ===========================================
    // P R O P E R T I E S
    // ===========================================

    private View in;
    public View out;

    // ===========================================
    // C T O R
    // ===========================================

    public Scamp(String n) {
        this.in = new View();
        this.out = new View();
    }

    @Override
    public Object clone() {
        Scamp scamp = null;
        try { scamp = (Scamp) super.clone(); }
        catch (CloneNotSupportedException e) {} // never happens
        // ...
        return scamp;
    }

    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public int degree() {
        return 0;
    }

    @Override
    public Node getNeighbor(int i) {
        return null;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        return false;
    }

    @Override
    public boolean contains(Node neighbor) {
        return false;
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    @Override
    public List<Node> getPeers() {
        return null;
    }

    @Override
    public String debug() {
        return null;
    }

    // ===========================================
    // P R I V A T E
    // ===========================================

}
