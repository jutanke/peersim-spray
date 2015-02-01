package example.scamp;

import example.cyclon.PeerSamplingService;
import peersim.core.CommonState;
import peersim.core.Node;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Inview and outview
 * Created by julian on 31/01/15.
 */
public class View {

    private final List<ViewEntry> array;
    private final List<Node> export;
    private final List<Node> exportFiltered;
    private final List<ViewEntry> normalized;

    private double totalWeight;

    public View() {
        this.array = new ArrayList<ViewEntry>();
        this.totalWeight = 0;
        this.export = new ArrayList<Node>();
        this.normalized = new ArrayList<ViewEntry>();
        this.exportFiltered = new ArrayList<Node>();
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

    public void del(Node n) {
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

    public List<Node> list(Node filter) {
        this.exportFiltered.clear();
        for (Node n : list()) {
            if (n.getID() != filter.getID()) {
                exportFiltered.add(n);
            }
        }
        return exportFiltered;
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
        if (index >= this.length()) {
            return false;
            //throw new RuntimeException("OOB! {updateWeight} " +
            //    " i:" + index + " size:" + length());
        }
        boolean isUpdated = (weight != this.get(index).weight);
        if (isUpdated) {
            this.totalWeight = this.totalWeight - this.get(index).weight + weight;
            this.get(index).weight = weight;
        }
        return isUpdated;
    }

    public boolean updateWeight(Node n, double weight) {
        return this.updateWeight(this.findPosition(n), weight);
    }

    public List<ViewEntry> normalizeWeights() {
        this.normalized.clear();
        for (int i = 0; i < this.array.size(); ++i) {
            if (this.updateWeight(i, this.get(i).weight / this.totalWeight)) {
                this.normalized.add(this.array.get(i));
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
        for (; i < this.array.size(); i++) {
            if (this.array.get(i).id == n.getID()) {
                break;
            }
        }
        return i;
    }

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static boolean SHOW_WEIGHT = false;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (ViewEntry e : this.array) {
            if (sb.length() > 1) sb.append(",");
            sb.append("{");
            sb.append(e.id);
            if (SHOW_WEIGHT) {
                sb.append("|w:");
                sb.append(df.format(e.weight));
            }
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public void clear() {
        this.export.clear();
        this.array.clear();
    }

    public List<Node> leaseTimeout() {
        this.exportFiltered.clear();
        for(ViewEntry e : this.array) {
            example.scamp.nohandshake.Scamp pp = (example.scamp.nohandshake.Scamp) e.node.getProtocol(
                    example.scamp.nohandshake.Scamp.pid
            );
            if (pp.isExpired()) {
                this.exportFiltered.add(e.node);
            }
        }
        return this.exportFiltered;
    }

    /**
     * *************************************
     * <p/>
     * **************************************
     */
    public class ViewEntry {
        public final long id;
        public long birthDate;
        public final Node node;
        public final long leaseTimeout;
        public double weight;

        public ViewEntry(Node n, Double w) {
            this.id = n.getID();
            this.node = n;
            this.weight = w;
            example.scamp.nohandshake.Scamp pp = (example.scamp.nohandshake.Scamp) n.getProtocol(
                    example.scamp.nohandshake.Scamp.pid
            );
            this.birthDate = pp.birthDate;
            this.leaseTimeout = pp.randomLeaseTimeout;
        }
    }

}
