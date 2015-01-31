package example.scamp;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by julian on 31/01/15.
 */
public abstract class ScampProtocol implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    // =================== static fields ==================================
    // ====================================================================


    /**
     * Parameter "c" of Scamp . Defaults to 0.
     *
     * @config
     */
    private static final String PAR_C = "c";

    private static final String SCAMP_PROT = "0";

    /**
     * Time-to-live for indirection. Defaults to -1.
     *
     * @config
     */
    private static final String PAR_INDIRTTL = "indirectionTTL";

    /**
     * Lease timeout. If negative, there is no lease mechanism. Defaults to -1.
     *
     * @config
     */
    private static final String PAR_LEASE = "leaseTimeout";

    private static final String PAR_TRANSPORT = "transport";

    /**
     * c
     */
    protected static int c;

    /**
     * indirection TTL
     */
    protected static int indirTTL;

    /**
     * lease timeout
     */
    protected static int leaseTimeout;

    protected final int tid;

    protected final int pid;

    /**
     *
     */
    protected int birthDate;

    protected Map<Long, Node> inView;
    protected Map<Long, Node> outView;

    private List<Node> outViewList;
    private List<Node> inViewList;


    public ScampProtocol(String n) {
        ScampProtocol.c = Configuration.getInt(n + "." + PAR_C, 0);
        ScampProtocol.indirTTL = Configuration.getInt(n + "." + PAR_INDIRTTL, -1);
        ScampProtocol.leaseTimeout = Configuration.getInt(n + "." + PAR_LEASE, -1);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        this.pid = Configuration.lookupPid(SCAMP_PROT);
        inView = new HashMap<Long, Node>();
        outView = new HashMap<Long, Node>();
        birthDate = CDState.getCycle();
        this.inViewList = new ArrayList<Node>();
        this.outViewList = new ArrayList<Node>();
        inView = new HashMap<Long, Node>();
        outView = new HashMap<Long, Node>();
    }

    public Object clone() {
        ScampProtocol p = null;
        try {
            p = (ScampProtocol) super.clone();
        } catch (CloneNotSupportedException e) {

        }
        p.outView = new HashMap<Long, Node>();
        p.inView = new HashMap<Long, Node>();
        p.inViewList = new ArrayList<Node>();
        p.outViewList = new ArrayList<Node>();
        return p;
    }

    /*
     * P U B L I C  I N T E R F A C E
     */

    @Override
    public int degree() {
        return this.outView.size();
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.outView.containsKey(neighbor.getID());
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    @Override
    public boolean addNeighbor(Node n) {
        return this.addToOutView(n);
    }

    @Override
    public Node getNeighbor(int i) {
        return this.getOutViewList().get(i);
    }

    @Override
    public List<Node> getPeers(){
        return this.getOutViewList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Out: [");
        for (Node n : this.outView.values()) {
            sb.append(" ");
            sb.append(n.getID());
        }
        sb.append("], In: [");
        for (Node n : this.inView.values()) {
            sb.append(" ");
            sb.append(n.getID());
        }
        sb.append("]");
        return sb.toString();
    }

    /*
     * I N T E R N A L  I N T E R F A C E
     */

    protected boolean isExpired() {
        return ((CDState.getCycle() - this.birthDate) > leaseTimeout);
    }

    protected boolean addToOutView (Node n) {
        if (this.outView.containsKey(n.getID())) {
            return false;
        } else {
            this.outView.put(n.getID(), n);
            return true;
        }
    }

    protected boolean addToInView(Node n) {
        if (this.inView.containsKey(n.getID())) {
            return false;
        } else {
            this.inView.put(n.getID(), n);
            return true;
        }
    }

    protected boolean p() {
        return CDState.r.nextDouble() < 1.0 / 1.0 + this.degree();
    }

    protected Node randomOutNode() {
        if (degree() > 0) {
            List<Node> out = this.getOutViewList();
            return out.get(CDState.r.nextInt(out.size()));
        }
        return null;
    }

    protected Node randomInNode() {
        if (degree() > 0) {
            List<Node> in = this.getInViewList();
            return in.get(CDState.r.nextInt(in.size()));
        }
        return null;
    }

    /**
     * @return INVIEW
     */
    protected List<Node> getInViewList() {
        this.inViewList.clear();
        for (Node e : this.inView.values()) {
            this.inViewList.add(e);
        }
        return this.inViewList;
    }

    /**
     * @return OUTVIEW
     */
    protected List<Node> getOutViewList() {
        this.outViewList.clear();
        for (Node e : this.outView.values()) {
            this.outViewList.add(e);
        }
        return this.outViewList;
    }

}
