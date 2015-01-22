package example.webrtc.cyclon;

import peersim.cdsim.CDProtocol;
import peersim.core.Linkable;
import peersim.core.Node;

/**
 * Created by julian on 22/01/15.
 */
public class CyclonProtocol implements CDProtocol, Linkable {

    // =============== static fields =======================================
    // =====================================================================


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
    public Object clone() {
        CyclonProtocol c = null;
        return c;
    }
}
