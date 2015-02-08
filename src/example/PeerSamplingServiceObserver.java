package example;

import example.scamp.ScampProtocol;
import example.webrtc.data.DictGraph;
import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 27/01/15.
 */
public class PeerSamplingServiceObserver implements Control {

    private static final String CYCLON_PROT = "lnk";
    private static final String SCAMP_PROT = "0";
    private static final String PAR_DEGREE = "DEGREE";

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    private int step = 0;

    /**
     * Protocol identifier
     */
    private final int pid;
    private final int degree;
    private final List<Long> peersWithEmptyCache;

    public PeerSamplingServiceObserver(String name) {
        int pid, degree = 0;
        try {
            pid = Configuration.lookupPid(CYCLON_PROT);
            degree = Configuration.getInt(PAR_DEGREE);
        } catch (MissingParameterException e) {
            pid = Configuration.lookupPid(SCAMP_PROT);
        }
        this.pid = pid;
        this.degree = degree;
        this.peersWithEmptyCache = new ArrayList<Long>();
    }

    @Override
    public boolean execute() {

        peersWithEmptyCache.clear();
        DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();

        double avgDegree = 0;
        int minDegree = Integer.MAX_VALUE, maxDegree = 0;

        for (int i = 0; i < Network.size(); i++) {

            GeneralNode n = (GeneralNode) Network.get(i);
            PeerSamplingService pss = (PeerSamplingService)
                    Network.get(i).getProtocol(pid);

            observer.add(n, pss);

            avgDegree += pss.getPeers().size();
            if (pss.getPeers().size() > maxDegree) {
                maxDegree = pss.getPeers().size();
            } else if (pss.getPeers().size() < minDegree) {
                minDegree = pss.getPeers().size();
            }

            //System.err.println("{" + n.getID() + "} -> " + pss.debug());

            if (pss.getPeers().size() == 0) {
                peersWithEmptyCache.add(n.getID());
            }


        }

        avgDegree /= Network.size();

        boolean histo = false;

        System.err.println("avg node degree:" + avgDegree + "(" + minDegree + "|" + maxDegree + ") arcs:" + observer.countArcs());
        //System.out.println(avgDegree);

        //System.out.println(observer.countArcs());

        //System.err.println(observer);

        //DictGraph.MeanPathLength mean = observer.meanPathLength();

        if (histo) {
            if (step == -1) {
                System.out.println("============== BABY ==============");
                printHistogram(observer);
            }

            if (step == 999) {
                System.out.println("============== Super END ==============");
                printHistogram(observer);
            }

            if (step == 499) {
                System.out.println("============== END ==============");
                printHistogram(observer);
            }

            if (step == -5) {
                System.out.println("============== START ==============");
                printHistogram(observer);
            }

            if (step == 250) {
                System.out.println("============== MIDDLE ==============");
                printHistogram(observer);
            }

            if (step == 1500) {
                System.out.println("============== SUPPPER END ==============");
                printHistogram(observer);
            }

            if (step == 1999) {
                System.out.println("============== SUPPPER++ END ==============");
                printHistogram(observer);
            }

            if (step == 2499) {
                System.out.println("============== SUPPPER++++ END ==============");
                printHistogram(observer);
            }
        }

        if (step % 100 == 0) {
            //double cluster = observer.meanClusterCoefficient();
            //System.out.println(cluster);
            //System.err.println("cluster: " + cluster);

            //double mean = observer.averagePathLength();
            //System.out.println(mean);
            //System.err.println("avg path len:" + mean);


            double avg1 = observer.avgReachablePaths(0).avg;
            double avg2 = observer.avgReachablePaths(40).avg;
            double avg3 = observer.avgReachablePaths(23).avg;
            double avg4 = observer.avgReachablePaths(644).avg;
            double avg5 = observer.avgReachablePaths(233).avg;
            double avg6 = observer.avgReachablePaths(22).avg;
            double avg7 = observer.avgReachablePaths(61).avg;
            double avg8 = observer.avgReachablePaths(721).avg;
            double avg9 = observer.avgReachablePaths(55).avg;
            double avg10 = observer.avgReachablePaths(86).avg;

            double avg = (avg1+avg2+avg3+avg4+avg5+avg6+avg7+avg8+avg9+avg10) / 10;

            System.out.println(avg);
            System.err.println("avg path :" + avg);


        }

        //System.err.println("reach :" + observer.avgReachablePaths(0).reachQuota);

        //double cluster = observer.meanClusterCoefficient();
        //System.out.println(cluster);
        //System.err.println("mean cluster:" + cluster);

        //DictGraph.AvgReachablePaths avg = observer.avgReachablePaths(0);
        //System.err.println("avg: " + avg);
        //System.out.println(avg.avg);

        //double cluster = observer.meanClusterCoefficient();
        //System.err.println("mean cluster:" + cluster);
        //System.out.println(cluster);

        if (step == 500 && false) {
            PeerSamplingService pss = (PeerSamplingService)
                    Network.get(0).getProtocol(pid);
            System.err.println(observer);

            System.err.println("HISTO: " );
            int[] h = observer.inDegreeAsHistogram();
            for (int i = 0; i < h.length; i++) {
                System.err.println(i + ":" + h[i]);
                System.out.println(h[i]);
            }

            //System.err.println("mean cluster:" + cluster);
        }

        //double cluster = observer.meanClusterCoefficient();
        //System.err.println("mean cluster:" + cluster);
        //System.out.println(cluster);

        //if (step == )

        System.err.println("step " + step + " => count:" + Network.size() +
                " orphans:" + peersWithEmptyCache.size() + " Cheating:" + ScampProtocol.CHEAT_COUNT);


        if (step == 1500) {
            //printGraph();
        }

        //System.out.println(avg.avg);

        //System.err.println(observer);


        step += 1;
        return false;
    }


    private void printHistogram(DictGraph observer) {
        int[] histo = observer.inDegreeAsHistogram();
        System.err.println("HISTOGRAM");
        for (int i = 0; i < histo.length; i++) {
            System.err.println(i + ":" + histo[i]);
            System.out.println(histo[i]);
        }
    }

    private void printGraph() {
        System.out.println("digraph {");
        for (int i = 0; i < Network.size(); i++) {
            GeneralNode n = (GeneralNode) Network.get(i);
            PeerSamplingService pss = (PeerSamplingService)
                    Network.get(i).getProtocol(pid);

            for (Node ne : pss.getPeers()) {
                System.out.println(n.getID() + " -> " + ne.getID() + ";");
            }
        }
        System.out.println("}");
    }
}
