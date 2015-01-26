package example.webrtc.cyclon2;

import example.webrtc.data.DictGraph;
import example.webrtc.data.Graph;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 26/01/15.
 */
public class CyclonObserver implements Control {

    private static final String CYCLON_PROT = "lnk";
    private static final String PAR_DEGREE = "DEGREE";

    /**
     * The protocol to operate on.
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /** Protocol identifier */
    private final int pid;
    private final int degree;
    private final List<Long> peersWithEmptyCache;

    public CyclonObserver(String name) {
        this.pid = Configuration.lookupPid(CYCLON_PROT);
        this.degree = Configuration.getInt(PAR_DEGREE);
        this.peersWithEmptyCache = new ArrayList<Long>();
    }


    @Override
    public boolean execute() {

        double avgDegree = 0;
        peersWithEmptyCache.clear();

        DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();

        for (int i = 0; i < Network.size(); i++) {

            GeneralNode n = (GeneralNode) Network.get(i);
            Cyclon cyclonNode = (Cyclon) Network.get(i).getProtocol(pid);

            avgDegree += cyclonNode.degree();

            observer.add(n, cyclonNode);

            if (cyclonNode.degree() == 0) {
                peersWithEmptyCache.add(n.getID());
            }

        }

        for (long key : observer.nodes.keySet()){
            System.err.println(key + " -> " + observer.nodes.get(key));
        }

        DictGraph.AvgReachablePaths p = observer.avgReachablePaths(0);

        System.err.println("CyclonObserver: dijkstra: " + p);

        System.err.println("CyclonObserver: Degree avg: " + avgDegree/Network.size());
        System.err.println("CyclonObserver: No peers in nodes: " + peersWithEmptyCache.toString());

        return false;
    }
}
