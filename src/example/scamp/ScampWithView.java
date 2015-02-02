package example.scamp;

import example.scamp.simple.*;
import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 01/02/15.
 */
public abstract class ScampWithView extends ScampProtocol {

    // ===================================================
    // E N T I T Y
    // ===================================================

    protected View inView;
    protected View partialView;

    protected long birthDate;

    public ScampWithView(String s) {
        super(s);
        this.inView = new View();
        this.partialView = new View();
        this.birthDate = CommonState.getTime();
    }

    @Override
    public Object clone() {
        ScampWithView s = (ScampWithView) super.clone();
        s.partialView = new View();
        s.inView = new View();
        s.birthDate = CommonState.getTime();
        return s;
    }

    // ===================================================
    // P U B L I C  I N T E R F A C E
    // ===================================================

    @Override
    public int degree() {
        return this.partialView.length();
    }

    @Override
    public Node getNeighbor(int i) {
        return this.partialView.get(i).node;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        return addToOutView(neighbour);
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.partialView.contains(neighbor);
    }

    @Override
    public List<Node> getPeers() {
        return this.partialView.list();
    }

    @Override
    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("{In:");
        sb.append(this.inView);
        sb.append(" Out:");
        sb.append(this.partialView);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void rejoin(Node me) {
        this.birthDate = CommonState.getTime();
        this.subRejoin(me, this.birthDate);
    }

    public abstract void subRejoin(Node me, long newBirthDate);

    @Override
    public void nextCycle(Node node, int protocolID) {

        if (this.isExpired() && this.degree() > 0) {
            this.rejoin(node);
        }

        this.subNextCycle(node);

        for (Node expired : this.inView.leaseTimeout()) {
            this.inView.del(expired);
            print("@" + node.getID() + " remove from inView: " + expired.getID());
        }
        for (Node expired : this.partialView.leaseTimeout()) {
            this.partialView.del(expired);
            print("@" + node.getID() + " remove from partialView: " + expired.getID());
        }

    }

    public abstract void subNextCycle(Node node);

    @Override
    public String toString(){
        return this.debug();
    }

    // ===================================================
    // I N T E R N A L  I N T E R F A C E
    // ===================================================

    public boolean isExpired() {
        long currentTime = CommonState.getTime();
        return (currentTime - this.birthDate) >= this.randomLeaseTimeout;
    }

    protected boolean addToOutView(Node n) {
        if (this.partialView.contains(n)) {
            this.partialView.updateBirthdate(n);
            return false;
        } else {
            this.partialView.add(n);
            return true;
        }
    }

    public boolean addToInView(Node n) {
        if (this.inView.contains(n)) {
            this.inView.updateBirthdate(n);
            return false;
        } else {
            this.inView.add(n);
            return true;
        }
    }

    public boolean p() {
        return CDState.r.nextDouble() < 1.0 / (1.0 + this.degree());
    }

    /**
     * Performs the indirection (a random walk) to get a random element from the
     * network. If the random walk gets stuck because of a node which is down,
     * the node which is down is returned. This models the fact that in the real
     * protocol in fact nothing is returned.
     */
    protected static Node getRandomNode(Node n) {

        if (true) {
            return Network.get(CommonState.r.nextInt(Network.size()));
        }

        ScampWithView pp = (ScampWithView) n.getProtocol(pid);
        if (false && pp.partialView.length() > 0) {
            return pp.partialView.get(CDState.r.nextInt(pp.degree())).node;
        } else {
            double ttl = indirTTL;
            ScampWithView l = (ScampWithView) n.getProtocol(pid);
            ttl -= 1.0 / l.degree();

            while (n.isUp() && ttl > 0.0) {
                if (l.degree() + l.inView.length() > 0) {
                    int id = CDState.r.nextInt(
                            l.degree() + l.inView.length());
                    if (id < l.degree()) n = l.getNeighbor(id);
                    else n = l.inView.get(id - l.degree()).node;
                } else break;

                l = (ScampWithView) n.getProtocol(pid);
                ttl -= 1.0 / l.degree();
            }

            if (ttl > 0.0) System.err.println("Scamp: getRandomNode returned with ttl=" + ttl);
            return n;
        }
    }
}
