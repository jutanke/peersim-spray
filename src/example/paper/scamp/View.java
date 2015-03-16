package example.paper.scamp;

import peersim.core.CommonState;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 3/16/15.
 */
public class View {

    private static final long leaseTimeoutMin = 1000;
    private static final long leaseTimeoutMax = 3000;

    // =====================================
    // P R O P E R T I E S
    // =====================================

    List<ViewEntry> array = new ArrayList<ViewEntry>();


    // =====================================
    // P U B L I C
    // =====================================

    public void updateTimeouts() {
        for (ViewEntry e : this.array) {

        }
    }

    public boolean add(Node n) {
        if (!this.contains(n)) {
            this.array.add(new ViewEntry(n));
            return true;
        }
        return false;
    }

    public List<Node> list() {
        final List<Node> result = new ArrayList<Node>();
        for (ViewEntry e : this.array) {
            result.add(e.node);
        }
        return result;
    }

    /**
     *
     * @param n
     * @return
     */
    public boolean contains(Node n) {
        return this.indexOf(n) >= 0;
    }

    /**
     *
     * @param n
     * @return
     */
    public int indexOf(Node n) {
        for (int i = 0; i < this.array.size(); i++) {
            if (this.array.get(i).node.getID() == n.getID()) {
                return i;
            }
        }
        return -1;
    }

    // =====================================
    // H E L P E R  C L A S S E S
    // =====================================

    public class ViewEntry {
        public final long id;
        public final long birthdate;
        public final Node node;
        public final long leaseTime;

        public ViewEntry(Node n) {
            this.node = n;
            this.id = n.getID();
            this.birthdate = CommonState.getTime();
            this.leaseTime = CommonState.r.nextLong(leaseTimeoutMax - leaseTimeoutMin) + leaseTimeoutMin;
        }

        public boolean timeout(){
            return ((this.birthdate + this.leaseTime) < CommonState.getTime());
        }
    }

}
