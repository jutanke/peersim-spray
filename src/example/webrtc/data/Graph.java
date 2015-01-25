package example.webrtc.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julian on 24/01/15.
 */
public class Graph {

    /* =================================================================== *
     * SINGLETON
     * =================================================================== */

    private static Graph singleton;

    public static Graph getSingleton(int size){
        if (singleton == null || singleton.size() != size) {
            singleton = new Graph(size);
        }
        return singleton;
    }

    /* =================================================================== *
     * PROPERTIES
     * =================================================================== */

    public final GraphNode[] nodes;

    private Graph(int size) {
        this.neighbourhood = new ArrayList<GraphNode>();
        this.dist = new int[size];
        this.prev = new int[size];
        this.Q = new LinkedList<GraphNode>();
        this.nodes = new GraphNode[size];
        for (int i = 0; i < size; i++) {
            this.nodes[i] = new GraphNode(i);
        }
    }

    /* =================================================================== *
     * PUBLIC
     * =================================================================== */

    public int size() { return this.nodes.length; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (GraphNode n1 : this.nodes) {
            sb.append("(");
            sb.append(n1.id);
            sb.append(")[");
            for (int n2 : n1.neighbors) {
                sb.append(n2);
                sb.append(",");
            }
            sb.append("]\n\r");
        }
        return sb.toString();
    }

    public double avgClusteringCoefficient() {
        double coefficientSum = 0;
        for(GraphNode v : nodes) {
            coefficientSum += localClusterCoefficient(v);
        }
        return (1.0/nodes.length) * coefficientSum;
    }

    public double avgPathLength(int v) {
        int[] dist = dijkstra(nodes[v]);

        double result = 0;
        for(int i : dist) {
            if (i != v) result += i * 0.01;
        }
        if (result < 0) return Double.MAX_VALUE;

        return result/dist.length * 100;
    }

    public AvgReachablePaths avgReachablePaths(int v) {
        int[] dist = dijkstra(nodes[v]);
        AvgReachablePaths result = new AvgReachablePaths();

        double sum = 0;
        int reachable = 0;
        for (int d : dist) {
            if (d != Integer.MAX_VALUE) {
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
        result.reachQuota = reachable / (dist.length -1);

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


    private int[] dist;
    private int[] prev;
    private LinkedList<GraphNode> Q;

    private int[] dijkstra(GraphNode src) {


        final int undefined = -1;
        int source = src.id;
        Q.clear();
        dist[source] = 0;
        prev[source] = undefined;
        for(GraphNode v : nodes) {
            if (v.id != source) {
                dist[v.id] = Integer.MAX_VALUE;
                prev[v.id] = undefined;
            }
            Q.add(v);
        }

        while (Q.size() > 0) {
            GraphNode u = min(Q,dist);
            Q.remove(u);
            for(int v : u.neighbors) {
                int alt = dist[u.id] + 1;
                if (alt < dist[v]) {
                    dist[v] = alt;
                    prev[v] = u.id;
                }
            }
        }
        return dist;
    };

    private GraphNode min(LinkedList<GraphNode> Q, int[] dist) {
        int m = Integer.MAX_VALUE;
        GraphNode min = null;
        for(GraphNode u : Q) {
            if (m >= dist[u.id]) {
                m = dist[u.id];
                min = u;
            }
        }
        return min;
    }

    private double localClusterCoefficient(GraphNode v) {
        List<GraphNode> N = neighbourhood(v);
        if (N.size() == 0) return 0;
        double possible = N.size() * (N.size() - 1);
        if (possible == 0) return 0;
        double actual = 0;
        for (GraphNode a : N) {
            for (GraphNode b : N) {
                if (a.id != b.id) {
                    if (areInterconnected(a.id, b.id)) {
                        actual += 1;
                    }
                }
            }
        }
        return actual/possible;
    }

    private List<GraphNode> neighbourhood;

    /**
     * get the immediately connected neighbours:
     * @param v
     * @return N_i = {v_j : e_ij \in E ^ e_ji \in E }
     */
    private List<GraphNode> neighbourhood(GraphNode v) {
        this.neighbourhood.clear();
        for (int n : v.neighbors) {
            if (hasDirectedConnection(n, v.id)) {
                this.neighbourhood.add(this.nodes[n]);
            }
        }
        return this.neighbourhood;
    }

    private boolean hasDirectedConnection(int from, int to) {
        GraphNode fromNode = this.nodes[from];
        if (in(to, fromNode.neighbors)) return true;
        return false;
    }

    private boolean areInterconnected(int a, int b) {
        GraphNode aNode = nodes[a];
        GraphNode bNode = nodes[b];
        return in(a, bNode.neighbors) && in(b, aNode.neighbors);
    }

    private boolean in(int i, List<Integer> list) {
        for(int n : list) {
            if (n == i) return true;
        }
        return false;
    }

 }
