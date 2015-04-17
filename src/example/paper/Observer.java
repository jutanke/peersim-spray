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
        int count = 0, disconnected = 0;

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
                if (size == 0) {
                    disconnected++;
                }
            }
        }
        System.err.println("MIN:" + min + ", MAX:" + max + ", count:" + count + ", disconnected:" + disconnected);


        //if (CommonState.getTime() > 0 ) System.out.println(observer.variancePartialView());
        //if (CommonState.getTime() > 0 ) System.out.println(observer.meanClusterCoefficient());
        //if (CommonState.getTime() > 0 ) System.out.println(observer.avgReachablePaths(0).reachQuota);
        //if (CommonState.getTime() > 2200) System.out.println(avgPathLength(observer));

        if (observer.size() % (Math.pow(10, Math.ceil(Math.log10(observer.size())))/2) == 0) {
            System.out.println(CommonState.getTime() + " " + avgPathLength(observer));
        }

        //DictGraph.ClusterResult clusterResult = observer.countClusters();
        //System.err.println(clusterResult);
        //System.out.println(clusterResult.count + " " + clusterResult.maxClusterSize);

        /*
        final int[] dist = observer.histogramPassiveWorkDistribution();
        int c = 0;
        for (int i : dist) {
            c += i;
        }
        System.err.println("exchanges:" + c);
        System.out.println("s--@" + CommonState.getTime());
        System.out.println(print(dist));
        System.out.println("e--@" + CommonState.getTime());
        */

        if (CommonState.getTime() == 149) {
            //System.err.println("qqq");
            //System.out.println(print(observer.inDegreeAsHistogram()));
            //System.out.println(observer.toGraph());

            //System.err.println("Avg path:" + avgPathLength(observer));
            //final int[] dup = observer.duplicates();
            //System.err.println(print(dup));
        }

        System.err.println("arc count:" + observer.countArcs());
        //System.out.println(observer.countArcs());

        if (CommonState.getTime() > 0 && CommonState.getTime() % 499 == 0) {
            //printArray(observer.inDegreeAsHistogram());
            //System.out.println("FINAL: " + avgPathLength(observer));
        }



        return false;
    }

    private String print(int[] list) {
        StringBuilder sb = new StringBuilder();
        for (int i : list) {
            sb.append(i);
            sb.append("\n");
        }
        return sb.toString();
    }

    private double avgPathLength(DictGraph observer) {
        System.err.print("avg path length: [");
        double total = 0;
        total += observer.avgReachablePaths(0).avg; //  1
        System.err.print("#");
        total += observer.avgReachablePaths(observer.size() / 2).avg; //  2
        System.err.print("#");
        total += observer.avgReachablePaths(observer.size() / 3).avg; //  3
        System.err.print("#");
        total += observer.avgReachablePaths(observer.size() / 4).avg; //  4
        System.err.print("#");
        total += observer.avgReachablePaths(observer.size() / 8).avg; //  5
        System.err.print("#");
        total += observer.avgReachablePaths(observer.size() / 16).avg; //  6
        System.err.print("#");
        total += observer.avgReachablePaths(observer.size() - 1).avg; //  7
        System.err.print("#");
        //total += observer.avgReachablePaths(randomId()).avg; //  8
        //System.err.println("#");
        //total += observer.avgReachablePaths(randomId()).avg; //  9
        //System.err.println("#");
        //total += observer.avgReachablePaths(randomId()).avg; // 10
        System.err.println("]");
        return total / 7.0;
    }

    /**
     *
     * @param observer
     * @return
     */
    private double avgPathLengthRand(DictGraph observer) {
        System.err.println("avg path length: [");
        double total = 0;
        total += observer.avgReachablePaths(randomId()).avg; //  1
        System.err.println("#");
        total += observer.avgReachablePaths(randomId()).avg; //  2
        System.err.println("#");
        total += observer.avgReachablePaths(randomId()).avg; //  3
        System.err.println("#");
        total += observer.avgReachablePaths(randomId()).avg; //  4
        System.err.println("#");
        total += observer.avgReachablePaths(randomId()).avg; //  5
        System.err.println("#");
        total += observer.avgReachablePaths(randomId()).avg; //  6
        System.err.println("#");
        total += observer.avgReachablePaths(randomId()).avg; //  7
        System.err.println("#");
        //total += observer.avgReachablePaths(randomId()).avg; //  8
        //System.err.println("#");
        //total += observer.avgReachablePaths(randomId()).avg; //  9
        //System.err.println("#");
        //total += observer.avgReachablePaths(randomId()).avg; // 10
        System.err.println("]");
        return total / 7.0;
    }

    private void printArray(int[] list) {
        System.out.println("array");
        for (int i = 0; i < list.length; i++) {
            System.out.println(list[i]);
        }
    }

    /**
     *
     * @return
     */
    private long randomId() {
        return Network.get(CommonState.r.nextInt(Network.size())).getID();
    }
}
