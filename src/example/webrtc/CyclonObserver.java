package example.webrtc;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;
import java.util.LinkedList;

/**
 * Created by julian on 24/01/15.
 */
public class CyclonObserver implements Control {

    private static final String CYCLON_PROT = "lnk";
    private static final String PAR_DEGREE = "DEGREE";

    private LinkedList<Long> peersWithEmptyCache;
    private int degree;
    private int minDegree;
    private long minDegreeNodeID;
    private int maxDegree;
    private double avgDegree;

    public CyclonObserver(String prefix) {
        peersWithEmptyCache = new LinkedList<Long>();
        degree = Configuration.getInt(PAR_DEGREE);
    }

    @Override
    public boolean execute() {

        int pid = Configuration.lookupPid(CYCLON_PROT);
        peersWithEmptyCache.clear();
        avgDegree = 0;
        minDegree = degree;
        maxDegree = 0;

        for (int i = 0; i < Network.size(); i++) {
            GeneralNode n = (GeneralNode) Network.get(i);
            CyclonProtocol cyclonNode = (CyclonProtocol) n.getProtocol(pid);
            int degree = cyclonNode.degree();
            avgDegree += degree;

            if(degree > maxDegree) {
                maxDegree = degree;
            }

            if(degree < minDegree) {
                minDegree = degree;
                minDegreeNodeID = n.getID();
            }

            if(degree == 0) {
                peersWithEmptyCache.add(n.getID());
            }
        }

        System.err.println("CyclonObserver: No peers in nodes: " + peersWithEmptyCache.toString());
        System.err.println("CyclonObserver: Degree avg: " + avgDegree/Network.size() + " min: " + minDegree + " max: " + maxDegree);
        System.err.println("CyclonObserver: Min degree at node: " + minDegreeNodeID);

        return false;
    }
}
