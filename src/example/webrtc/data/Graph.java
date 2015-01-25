package example.webrtc.data;

import java.util.ArrayList;
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

    public double clusteringCoefficient() {
        double coefficient = 0;




        return coefficient;
    }

    /* =================================================================== *
     * PRIVATE
     * =================================================================== */

    private List<GraphNode> neighbourhood;

    /**
     * get the immediately connected neihbours:
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
        for(int n : fromNode.neighbors) {
            if (n == to) return true;
        }
        return false;
    }

 }
