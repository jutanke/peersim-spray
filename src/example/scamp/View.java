package example.scamp;

import example.cyclon.PeerSamplingService;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Inview and outview
 * Created by julian on 31/01/15.
 */
public class View {

    private final List<ViewEntry> array;
    private final List<Node> export;
    private final List<Node> normalized;

    private double totalWeight;

    public View() {
        this.array = new ArrayList<ViewEntry>();
        this.totalWeight = 0;
        this.export = new ArrayList<Node>();
        this.normalized = new ArrayList<Node>();
    }

    public int length() {
        return this.array.size();
    }

    public void add(Node n) {
        double weight = 1;
        if (this.array.size() > 0) {
            weight = this.totalWeight / this.array.size();
        }
        this.totalWeight += weight;
        this.array.add(new ViewEntry(n, weight));
        this.export.add(n);
    }

    public void del(int index) {
        this.totalWeight -= this.array.get(index).weight;
        this.array.remove(index);
        this.export.remove(index);
    }

    public void del(Node n){
        int i = 0;
        for (; i < this.array.size(); i++) {
            if (this.array.get(i).id == n.getID()) {
                break;
            }
        }
        if (i < this.array.size()) {
            this.del(i);
        }
    }

    public List<Node> list() {
        return this.export;
    }

    public boolean contains(Node s) {
        for (Node n : this.list()) {
            if (n.getID() == s.getID()) {
                return true;
            }
        }
        return false;
    }

    public ViewEntry get(int i) {
        return this.array.get(i);
    }

    public ViewEntry get(Node n) {
        return get(this.findPosition(n));
    }

    public boolean updateWeight(int index, double weight) {
        boolean isUpdated = (weight != this.get(index).weight);
        if (isUpdated) {
            this.totalWeight = this.totalWeight - this.get(index).weight + weight;
            this.get(index).weight = weight;
        }
        return isUpdated;
    }

    public boolean updateWeight(Node n, double weight){
        return this.updateWeight(this.findPosition(n), weight);
    }

    public List<Node> normalizeWeights() {
        boolean isUpdated;
        this.normalized.clear();
        for (int i = 0; i < this.array.size(); ++i) {
            if (this.updateWeight(i, this.get(i).weight / this.totalWeight)) {
                this.normalized.add(this.array.get(i).node);
            }
        }
        return this.normalized;
    }

    public double getWeight(int i) {
        return this.array.get(i).weight;
    }

    public double getWeight(Node n) {
        return this.getWeight(this.findPosition(n));
    }


    private int findPosition(Node n) {
        int i = 0;
        for (;i<this.array.size();i++) {
            if (this.array.get(i).id == n.getID()) {
                break;
            }
        }
        return i;
    }

    /****************************************
     *
     ****************************************/
    public class ViewEntry {
        public final long id;
        public final Node node;
        public double weight;
        public ViewEntry(Node n, Double w) {
            this.id = n.getID();
            this.node = n;
            this.weight = w;
        }
    }

}
