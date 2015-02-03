package example.scampXcyclon;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.List;

/**
 * Created by julian on 2/3/15.
 */
public class Scamplon implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    public static int c, tid, pid;

    // ============================================
    // E N T I T Y
    // ============================================

    private static final String PAR_C = "c";
    private static final String SCAMPLON_PROT = "0";
    private static final String PAR_TRANSPORT = "transport";

    public Scamplon(String n) {
        c = Configuration.getInt(n + "." + PAR_C, 0);
        tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        pid = Configuration.lookupPid(SCAMPLON_PROT);
    }

    @Override
    public Object clone(){
        Scamplon s = null;
        try {
            s = (Scamplon) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return s;
    }

    // ============================================
    // P U B L I C
    // ============================================

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

    // ============================================
    // P R I V A T E
    // ============================================
}
