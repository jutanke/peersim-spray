package example.scamp.orig;

import example.scamp.ScampProtocol;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 01/02/15.
 */
public class Scamp extends ScampProtocol {



    // ===================================================
    // E N T I T Y
    // ===================================================

    public Scamp(String s) { super(s);}

    // ===================================================
    // P U B L I C  I N T E R F A C E
    // ===================================================

    @Override
    public void join(Node me, Node subscriber) {

    }

    @Override
    public void rejoin(Node me) {

    }

    @Override
    public void unsubscribe(Node me) {

    }

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

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
    public List<Node> getPeers() {
        return null;
    }

    @Override
    public String debug() {
        return null;
    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================
}
