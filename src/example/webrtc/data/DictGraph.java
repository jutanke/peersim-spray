package example.webrtc.data;

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
    private final Map<Long, Integer> dist;
    private final LinkedList<DictNode> Q;

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

    public void add(Node n, example.PeerSamplingService c) {
        DictNode node = new DictNode(n.getID());
        for (Node neighbor : c.getPeers()) {
            node.neighbors.add(neighbor.getID());
        }
        if (this.nodes.containsKey(n.getID())) throw new Error("should never happen");
        this.nodes.put(n.getID(), node);
    }

    public AvgReachablePaths avgReachablePaths(long v) {
        //Map<Long, Integer> dist = dijkstra(nodes.get(v));
        Map<Long, Integer> dist = dijkstraUndirected(nodes.get(v).id);

        AvgReachablePaths result = new AvgReachablePaths();

        double sum = 0;
        int reachable = 0;
        for (int d : dist.values()) {
            if (d != -1.0) {
                reachable += 1;
                sum += (double) d;
            }
        }

        if (reachable <= 1) {
            result.avg = 0;
        } else {
            result.avg = sum / (reachable - 1); // do not count ourselfs!
        }

        result.count = reachable;
        result.total = this.nodes.size();
        result.reachQuota = reachable / (double) result.total;


        return result;
    }

    public class AvgReachablePaths {
        public double avg;
        public int count;
        public int total;
        public double reachQuota;

        @Override
        public String toString() {
            return "avg:" + avg + "| %:" + reachQuota + " |count:" + this.count + " |total:" + total;
        }
    }

    public MeanPathLength meanPathLength() {
        MeanPathLength result = new MeanPathLength();
        result.maxReachQuota = Double.MIN_VALUE;
        result.minReachQuota = Double.MAX_VALUE;

        for (DictNode n : this.nodes.values()) {
            AvgReachablePaths avg = avgReachablePaths(n.id);
            result.avg += avg.avg;
            result.reachQuota += avg.reachQuota;
            if (avg.reachQuota > result.maxReachQuota) {
                result.maxReachQuota = avg.reachQuota;
            }
            if (avg.reachQuota < result.minReachQuota) {
                result.minReachQuota = avg.reachQuota;
            }
        }

        result.avg /= this.nodes.size();
        result.reachQuota /= this.nodes.size();

        return result;
    }

    public class MeanPathLength {
        public double avg;
        public double reachQuota;
        public double minReachQuota;
        public double maxReachQuota;

        @Override
        public String toString() {
            return "avg:" + avg + "| %:" + reachQuota + "| min%:" + minReachQuota + "| max%:" + maxReachQuota;
        }
    }

    public double averagePathLength() {

        // id1_id2 : Distance between 1 and 2
        Map<String, Integer> d = new HashMap<String, Integer>();

        Map<Long, Map<Long, Integer>> lookup = new HashMap<Long, Map<Long, Integer>>();

        for (DictNode e : this.nodes.values()) {
            lookup.put(e.id, dijkstra(e));
        }

        for (DictNode a : this.nodes.values()) {
            for (DictNode b : this.nodes.values()) {
                String key = key(a.id, b.id);
                if (a.id != b.id) {
                    int d1 = lookup.get(a.id).get(b.id);
                    int d2 = lookup.get(b.id).get(a.id);
                    if (d1 == -1 && d2 == -1) d.put(key, 0);
                    else if (d1 == -1) d.put(key, d2);
                    else if (d2 == -1) d.put(key, d1);
                    else d.put(key, Math.min(d1, d2));
                } else {
                    d.put(key, 0);
                }
            }
        }

        // =======================================

        int n = this.nodes.size();

        if (n > 1) {
            int sum = 0;
            for (int distance : d.values()) {
                sum += distance;
            }
            return (2.0 / n * (n * 1.0)) * (double) sum;
        }
        return 0;
    }

    private String key(long id1, long id2) {
        return Math.min(id1, id2) + "_" + Math.max(id1, id2);
    }

    @Override
    public String toString() {
        //return this.nodes.toString();
        StringBuilder sb = new StringBuilder();

        for (DictNode n : this.nodes.values()) {
            sb.append("{");
            sb.append(n.id);
            sb.append("}->[");
            for (long neighbor : n.neighbors) {
                sb.append(" ");
                sb.append(neighbor);
            }
            sb.append("]\n\r");
        }

        return sb.toString();
    }

    public double meanClusterCoefficient() {
        double sum = 0;
        for (DictNode e : this.nodes.values()) {
            sum += localClusterCoefficient(e);
        }
        return sum / this.nodes.size();
    }

    // =========


    public int[] inDegrees () {
        // ====== #1 Count the in-degree of all peers ======
        Map<Long, Integer> lookup = new HashMap<Long, Integer>(this.nodes.size());
        for (DictNode e : this.nodes.values()) {
            lookup.put(e.id, 0);
        }
        for (DictNode e : this.nodes.values()) {
            for (long n : e.neighbors) {
                lookup.put(n, lookup.get(n) + 1);
            }
        }

        // ====== #2 put the numers in a list ======
        List<Integer> degs = new ArrayList<Integer>(lookup.size());
        for (int deg : lookup.values()) {
            degs.add(deg);
        }
        int[] result = new int[lookup.size()];
        for (int i = 0; i < degs.size();i++) result[i] = degs.get(i);
        return result;
    }

    public int[] outDegreeAsHistogram() {
        // ====== #1 Count the in-degree of all peers ======
        Map<Long, Integer> lookup = new HashMap<Long, Integer>(this.nodes.size());
        for (DictNode e : this.nodes.values()) {
            lookup.put(e.id, 0);
        }
        for (DictNode e : this.nodes.values()) {
            for (long n : e.neighbors) {
                lookup.put(n, lookup.get(n) + 1);
            }
        }

        // ====== #3 create histogram for the counts ======
        Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
        for (int inDegree : lookup.values()) {
            if (histogram.containsKey(inDegree)) {
                histogram.put(inDegree, histogram.get(inDegree) + 1);
            } else {
                histogram.put(inDegree, 1);
            }
        }

        // ====== #3 turn the lookup into an array ======
        int max = 0;
        for (int deg : histogram.keySet()) {
            if (deg > max) max = deg;
        }
        int[] result = new int[max + 1];
        for (int deg : histogram.keySet()) {
            result[deg] = histogram.get(deg);
        }
        return result;
    }

    /**
     * @return index of array = number of in-degree
     */
    public int[] inDegreeAsHistogram() {

        // ====== #1 Count the in-degree of all peers ======
        Map<Long, Integer> lookup = new HashMap<Long, Integer>(this.nodes.size());
        for (DictNode e : this.nodes.values()) {
            lookup.put(e.id, 0);
        }
        for (DictNode e : this.nodes.values()) {
            for (long n : e.neighbors) {
                lookup.put(n, lookup.get(n) + 1);
            }
        }

        // ====== #3 create histogram for the counts ======
        Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
        for (int inDegree : lookup.values()) {
            if (histogram.containsKey(inDegree)) {
                histogram.put(inDegree, histogram.get(inDegree) + 1);
            } else {
                histogram.put(inDegree, 1);
            }
        }

        // ====== #3 turn the lookup into an array ======
        int max = 0;
        for (int deg : histogram.keySet()) {
            if (deg > max) max = deg;
        }
        int[] result = new int[max + 1];
        for (int deg : histogram.keySet()) {
            result[deg] = histogram.get(deg);
        }
        return result;
    }

    /* =================================================================== *
     * PRIVATE
     * =================================================================== */


    public double localClusterCoefficient(long id) {
        return localClusterCoefficient(nodes.get(id));
    }

    public double localClusterCoefficient(DictNode v) {
        //List<DictNode> N = neighbourhood(v);
        HashSet<Long> N = neighbors(v.id);
        if (N.size() == 0) return 0;
        double possible = N.size() * (N.size() - 1);
        if (possible == 0) return 0;
        double actual = 0;
        for (long a : N) {
            for (long b : N) {
                if (a != b) {
                    if (areUndirectlyConnected(a, b)) {
                        actual += 1;
                    }
                }
            }
        }
        return actual / possible;
    }

    private boolean areUndirectlyConnected(long a, long b) {
        DictNode aNode = nodes.get(a);
        DictNode bNode = nodes.get(b);
        return in(a, bNode.neighbors) || in(b, aNode.neighbors);
    }

    private boolean areInterconnected(long a, long b) {
        DictNode aNode = nodes.get(a);
        DictNode bNode = nodes.get(b);
        return in(a, bNode.neighbors) && in(b, aNode.neighbors);
    }

    /**
     * get the immediately connected neighbours:
     *
     * @param v
     * @return N_i = {v_j : e_ij \in E ^ e_ji \in E }
     */
    public List<DictNode> neighbourhood(DictNode v) {
        this.neighbourhood.clear();
        //for (long n : v.neighbors) {
        for (long n : neighbors(v.id)) {
            if (hasDirectedConnection(n, v.id)) {
                this.neighbourhood.add(this.nodes.get(n));
            }
        }
        return this.neighbourhood;
    }

    private boolean hasDirectedConnection(long from, long to) {
        DictNode fromNode = this.nodes.get(from);
        return in(to, fromNode.neighbors);
    }

    private boolean in(long i, List<Long> list) {
        for (long n : list) {
            if (n == i) return true;
        }
        return false;
    }

    public Map<Long, Integer> dijkstra(DictNode src) {
        return dijkstra(src.id);
    }

    /**
     * @param src
     * @return
     */
    public Map<Long, Integer> dijkstra(long src) {
        dist.clear();
        Q.clear();

        final long source = src;
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
            if (u == null) break; // disconnected graph
            Q.remove(u);
            for (long v : u.neighbors) {
                int alt = dist.get(u.id) + 1;
                if (dist.get(v) == INFINITY || alt < dist.get(v)) {
                    dist.put(v, alt);
                }
            }
        }
        return dist;
    }


    public Map<Long, Integer> dijkstraUndirected(final long source) {
        dist.clear();
        Q.clear();

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
            if (u == null) break; // disconnected graph
            Q.remove(u);
            for (long v : neighbors(u.id)) {
                int alt = dist.get(u.id) + 1;
                if (dist.get(v) == INFINITY || alt < dist.get(v)) {
                    dist.put(v, alt);
                }
            }
        }

        return dist;
    }

    private HashSet<Long> neighbors(final long id) {
        final HashSet<Long> result = new HashSet<Long>(this.nodes.get(id).neighbors);
        for (DictNode e : this.nodes.values()) {
            if (e.id != id && !result.contains(e.id)) {
                for (long n : e.neighbors) {
                    if (n == id) {
                        result.add(e.id);
                    }
                }
            }
        }
        return result;
    }

    private DictNode min(LinkedList<DictNode> Q, Map<Long, Integer> dist) {
        int m = Integer.MAX_VALUE;
        DictNode min = null;
        for (DictNode u : Q) {
            int distance = dist.get(u.id);
            if (distance >= 0 && m >= distance) {
                m = distance;
                min = u;
            }
        }
        //if (min == null && Q.size() > 0) {
        //    return Q.get(0);
        //}
        return min;
    }


}
