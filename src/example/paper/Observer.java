package example.paper;

import example.PeerSamplingService;
import example.paper.scamplon.Scamplon;
import example.webrtc.data.DictGraph;
import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.*;

/**
 * Created by julian on 3/15/15.
 */
public class Observer implements Control {

    private static final String PROTOCOL = "lnk";
    private static final String PROTOCOL_0 = "0";

    // =============================================
    // C T O R
    // =============================================

    private int pid;

    public Observer(String name) {

        try {
            this.pid = Configuration.lookupPid(PROTOCOL);
        } catch (MissingParameterException e) {
            this.pid = Configuration.lookupPid(PROTOCOL_0);
        }

    }

    // =============================================
    // E X E C
    // =============================================

    @Override
    public boolean execute() {

        final int STEP = 10;
        final DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();

        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int count = 0;

        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            Dynamic d = (Dynamic) n.getProtocol(pid);
            if (d.isUp()) {
                count += 1;
                PeerSamplingService pss = (PeerSamplingService) n.getProtocol(pid);
                observer.add(n, pss);
                final int size = pss.getPeers().size();
                if (size < min) {
                    min = size;
                }
                if (size > max) {
                    max = size;
                }
            }
        }
        System.err.println("MIN:" + min + ", MAX:" + max + " count:" + count);

        if (CommonState.getTime() % 1 == 0) {
            if (count > 0) {
                //System.out.println(observer.avgReachablePaths(0).reachQuota);
                //System.out.println(avgPathLength(observer));
                //System.out.println(observer.meanClusterCoefficient());
                //System.out.println(((double)count) / (double)Network.size());
            } else {
                System.out.println(0);
            }
        }

        System.out.println(observer.countArcs());

        if (CommonState.getTime() > 0 && CommonState.getTime() % 999 == 0) {
            //System.out.println(observer.toGraph());
        }



        return false;
    }

    /**
     *
     * @param observer
     * @return
     */
    private double avgPathLength(DictGraph observer) {
        double total = 0;
        total += observer.avgReachablePaths(randomId()).avg; //  1
        total += observer.avgReachablePaths(randomId()).avg; //  2
        total += observer.avgReachablePaths(randomId()).avg; //  3
        total += observer.avgReachablePaths(randomId()).avg; //  4
        total += observer.avgReachablePaths(randomId()).avg; //  5
        total += observer.avgReachablePaths(randomId()).avg; //  6
        total += observer.avgReachablePaths(randomId()).avg; //  7
        total += observer.avgReachablePaths(randomId()).avg; //  8
        total += observer.avgReachablePaths(randomId()).avg; //  9
        total += observer.avgReachablePaths(randomId()).avg; // 10
        return total / 10.0;
    }

    /**
     *
     * @return
     */
    private long randomId() {
        return Network.get(CommonState.r.nextInt(Network.size())).getID();
    }
}
