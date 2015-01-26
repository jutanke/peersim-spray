package example.webrtc.data;

import example.webrtc.cyclon2.Cyclon;
import peersim.core.Node;

import java.util.*;

/**
 * Created by julian on 26/01/15.
 */
public class DictGraph {

    /* =================================================================== *
     * SINGLETON
     * =================================================================== */

    private static DictGraph singleton;

    public static DictGraph getSingleton(int size) {
        if (singleton == null) {
            singleton = new DictGraph(size);
        }
        return singleton;
    }

    /* =================================================================== *
     * PROPERTIES
     * =================================================================== */

    //public final GraphNode[] nodes;
    public final Map<Long, DictNode> nodes;

    private List<DictNode> neighbourhood;

    private DictGraph(int size) {
        this.neighbourhood = new ArrayList<DictNode>();
        this.dist = new HashMap<Long, Integer>();
        //this.prev = new int[size];
        this.Q = new LinkedList<DictNode>();
        this.nodes = new HashMap<Long, DictNode>(size);
    }

    /* =================================================================== *
     * PUBLIC
     * =================================================================== */


    public void reset() {
        this.nodes.clear();
    }

    public void add(Node n, Cyclon c) {
        DictNode node = new DictNode(n.getID());
        for (Node neighbor : c.getNeighbors()) {
            node.neighbors.add(neighbor.getID());
        }
        if (this.nodes.containsKey(n.getID())) throw new Error("should never happen");
        this.nodes.put(n.getID(), node);
    }

    public AvgReachablePaths avgReachablePaths(long v) {
        Map<Long, Integer> dist = dijkstra(nodes.get(v));
        AvgReachablePaths result = new AvgReachablePaths();

        double sum = 0;
        int reachable = 0;
        for (int d : dist.values()) {
            if (d != -1) {
                reachable += 1;
                sum += (double)d;
            }
        }

        if (reachable == 0) {
            result.avg = Double.MAX_VALUE;
        } else {
            result.avg = sum / reachable;
        }

        result.count = reachable;
        result.reachQuota = reachable / (dist.size() -1);

        return result;
    }

    public class AvgReachablePaths {
        public double avg;
        public int count;
        public double reachQuota;

        @Override
        public String toString(){
            return "avg:" + avg + "| %:" + reachQuota;
        }
    }

    /* =================================================================== *
     * PRIVATE
     * =================================================================== */

    private final Map<Long, Integer> dist;
    private final LinkedList<DictNode> Q;

    private Map<Long, Integer> dijkstra(DictNode src) {
        dist.clear();
        Q.clear();

        final long source = src.id;
        final int INFINITY = -1;

        dist.put(source, 0);

        for (DictNode v : nodes.values()) {
            if (v.id != source) {
                dist.put(v.id, INFINITY);
            }
            Q.add(v);
        }

        while (Q.size() > 0) {
            DictNode u = min(Q, dist);
            Q.remove(u);
            for(long v : u.neighbors) {
                int alt = dist.get(u.id) + 1;
                if (alt < dist.get(v)) {
                    dist.put(v,alt);
                }
            }
        }
        return dist;
    }

    private DictNode min(LinkedList<DictNode> Q, Map<Long, Integer> dist) {
        int m = Integer.MAX_VALUE;
        DictNode min = null;
        for (DictNode u : Q) {
            if (m >= dist.get(u.id)) {
                m = dist.get(u.id);
                min = u;
            }
        }
        return min;
    }


}
