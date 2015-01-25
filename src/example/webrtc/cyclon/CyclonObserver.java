package example.webrtc.cyclon;

import example.webrtc.data.Graph;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julian on 24/01/15.
 */
public class CyclonObserver implements Control {

    private static final String CYCLON_PROT = "lnk";
    private static final String PAR_DEGREE = "DEGREE";

    private LinkedList<Long> peersWithEmptyCache;
    private List<Integer> lonely;
    private int degree;
    private int minDegree;
    private long minDegreeNodeID;
    private int maxDegree;
    private double avgDegree;

    public CyclonObserver(String prefix) {
        peersWithEmptyCache = new LinkedList<Long>();
        lonely = new ArrayList<Integer>();
        degree = Configuration.getInt(PAR_DEGREE);
    }

    @Override
    public boolean execute() {

        int pid = Configuration.lookupPid(CYCLON_PROT);
        peersWithEmptyCache.clear();
        lonely.clear();
        avgDegree = 0;
        minDegree = degree;
        maxDegree = 0;

        Graph observer = Graph.getSingleton(Network.size());

        for (int i = 0; i < Network.size(); i++) {
            GeneralNode n = (GeneralNode) Network.get(i);
            CyclonProtocol cyclonNode = (CyclonProtocol) n.getProtocol(pid);
            int degree = cyclonNode.degree();
            avgDegree += degree;

            observer.nodes[i].reset();
            // add all the neighbors!
            for(int id : cyclonNode.activeCache) {
                observer.nodes[i].neighbors.add(id);
            }

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

            if (cyclonNode.lonely()) {
                lonely.add(n.getIndex());
            }
        }

        //System.out.println(observer.avgClusteringCoefficient());
        //System.out.println(observer.avgPathLength(0));

        Graph.AvgReachablePaths p = observer.avgReachablePaths(0);
        System.out.println(p.avg);
        System.err.println(p);

        System.err.println("CyclonObserver: Lonely: " + lonely.toString());
        System.err.println("CyclonObserver: No peers in nodes: " + peersWithEmptyCache.toString());
        System.err.println("CyclonObserver: Degree avg: " + avgDegree/Network.size() + " min: " + minDegree + " max: " + maxDegree);
        System.err.println("CyclonObserver: Min degree at node: " + minDegreeNodeID);

        return false;
    }
}
