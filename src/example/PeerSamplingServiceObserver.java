package example;

import example.scamp.ScampProtocol;
import example.webrtc.data.DictGraph;
import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

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

        for (int i = 0; i < Network.size(); i++) {

            GeneralNode n = (GeneralNode) Network.get(i);
            PeerSamplingService pss = (PeerSamplingService)
                    Network.get(i).getProtocol(pid);

            observer.add(n, pss);

            //System.err.println("{" + n.getID() + "} -> " + pss.debug());

            if (pss.getPeers().size() == 0) {
                peersWithEmptyCache.add(n.getID());
            }


        }

        boolean histo = false;

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

        DictGraph.AvgReachablePaths avg = observer.avgReachablePaths(0);

        if (step == 100) {
            PeerSamplingService pss = (PeerSamplingService)
                    Network.get(0).getProtocol(pid);
            System.err.println(observer);

            double cluster = observer.meanClusterCoefficient();
            System.err.println("mean cluster:" + cluster);
        }

        //double cluster = observer.meanClusterCoefficient();
        //System.err.println("mean cluster:" + cluster);
        //System.out.println(cluster);

        //if (step == )

        System.err.println("step " + step + " => count:" + Network.size() + " orphans:" + peersWithEmptyCache.size());

        System.err.println("avg: " + avg);

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
}
