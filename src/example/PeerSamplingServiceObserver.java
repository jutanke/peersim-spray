package example;

import example.webrtc.cyclon2.Cyclon;
import example.webrtc.data.DictGraph;
import peersim.config.Configuration;
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
    private static final String PAR_DEGREE = "DEGREE";

    /**
     * The protocol to operate on.
     * @config
     */
    private static final String PAR_PROT = "protocol";

    private int step = 0;

    /** Protocol identifier */
    private final int pid;
    private final int degree;
    private final List<Long> peersWithEmptyCache;

    public PeerSamplingServiceObserver(String name) {
        this.pid = Configuration.lookupPid(CYCLON_PROT);
        this.degree = Configuration.getInt(PAR_DEGREE);
        this.peersWithEmptyCache = new ArrayList<Long>();
    }

    @Override
    public boolean execute() {

        peersWithEmptyCache.clear();
        DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();

        for (int i = 0; i < Network.size(); i++) {

            GeneralNode n = (GeneralNode) Network.get(i);
            example.cyclon.PeerSamplingService pss = (example.cyclon.PeerSamplingService)
                    Network.get(i).getProtocol(pid);

            observer.add(n, pss);

            if (pss.getPeers().size() == 0) {
                peersWithEmptyCache.add(n.getID());
            }

        }

        //System.err.println(observer);

        //DictGraph.MeanPathLength mean = observer.meanPathLength();
        //System.out.println(mean.avg);

        System.err.println("step " + step + " => count:" + Network.size() + " orphans:" + peersWithEmptyCache.size());

        if (step == 499) {
            System.out.println("============== END ==============");
            printHistogram(observer);
        }

        if (step == 5) {
            System.out.println("============== START ==============");
            printHistogram(observer);
        }

        if (step == 250) {
            System.out.println("============== MIDDLE ==============");
            printHistogram(observer);
        }

        //DictGraph.AvgReachablePaths avg = observer.avgReachablePaths(0);

        //double cluster = observer.meanClusterCoefficient();
        //System.err.println("mean cluster:" + cluster);

        //System.out.println(cluster);

        //if (step == )

        //System.err.println("avg: " + avg);

        //System.out.println(avg.avg);

        //System.err.println(observer);

        //System.out.println(observer.meanClusterCoefficient());

        //System.err.println("observer " + mean);
        //System.err.println("observer avg-path-len:" + observer.averagePathLength());

        step += 1;
        return false;
    }


    private void printHistogram(DictGraph observer) {
        int[] histo = observer.inDegreeAsHistogram();
        System.err.println("HISTOGRAM");
        for (int i = 0; i < histo.length; i++) {
            System.err.println( i + ":" + histo[i]);
            System.out.println(histo[i]);
        }
    }
}
