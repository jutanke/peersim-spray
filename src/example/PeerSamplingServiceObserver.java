package example;

import example.cyclon.PeerSamplingService;
import example.webrtc.cyclon2.Cyclon;
import example.webrtc.data.DictGraph;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 26/01/15.
 */
public class PeerSamplingServiceObserver implements Control {

    private static final String CYCLON_PROT = "lnk";
    private static final String PAR_DEGREE = "DEGREE";

    /**
     * The protocol to operate on.
     * @config
     */
    private static final String PAR_PROT = "protocol";

    // ======================================================================
    // P R O P E R T I E S
    // ======================================================================

    /** Protocol identifier */
    private final int pid;
    private final int degree;
    private final List<Long> peersWithEmptyCache;

    public PeerSamplingServiceObserver(String name) {
        this.pid = Configuration.lookupPid(CYCLON_PROT);
        this.degree = Configuration.getInt(PAR_DEGREE);
        this.peersWithEmptyCache = new ArrayList<Long>();
    }

    // ======================================================================
    // P R I V A T E  I N T E R F A C E
    // ======================================================================

    @Override
    public boolean execute() {

        DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();
        peersWithEmptyCache.clear();

        double avgDegree = 0;

        for (int i = 0; i < Network.size(); i++) {

            GeneralNode n = (GeneralNode) Network.get(i);
            PeerSamplingService cyclonNode = (PeerSamplingService) Network.get(i).getProtocol(pid);

            avgDegree += cyclonNode.getPeers().size();

            if (cyclonNode.getPeers().size() == 0) {
                peersWithEmptyCache.add(n.getID());
            }

        }

        System.err.println("CyclonObserver: Degree avg: " + avgDegree/Network.size());
        System.err.println("CyclonObserver: No peers in nodes: " + peersWithEmptyCache.toString());

        return false;
    }
}
