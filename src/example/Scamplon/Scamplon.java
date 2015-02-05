package example.Scamplon;

import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 2/5/15.
 */
public class Scamplon extends ScamplonProtocol {

    // ============================================
    // E N T I T Y
    // ============================================


    public Scamplon(String s) {
        super(s);
    }

    // ============================================
    // P U B L I C
    // ============================================

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

    // ============================================
    // P R I V A T E
    // ============================================
}
