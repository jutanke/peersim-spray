package example.scamp.handshake;

import example.cyclon.PeerSamplingService;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 29/01/15.
 */
public class ScampHandshake implements CDProtocol, EDProtocol, Linkable, PeerSamplingService  {

    // =================== static fields ==================================
    // ====================================================================

    /**
     * Parameter "c" of Scamp . Defaults to 0.
     * @config
     */
    private static final String PAR_C = "c";

    /**
     * Time-to-live for indirection. Defaults to -1.
     * @config
     */
    private static final String PAR_INDIRTTL = "indirectionTTL";

    /**
     * Lease timeout. If negative, there is no lease mechanism. Defaults to -1.
     * @config
     */
    private static final String PAR_LEASE = "leaseTimeout";

    /** c */
    private static int c;

    /** indirection TTL */
    private static int indirTTL;

    /** lease timeout */
    private static int leaseTimeout;

    // ======================================================================
    // P R O P E R T I E S
    // ======================================================================

    private ArrayList<Node> outView;

    /**
     * Contains creation dates of elements in outView, if leasing is used.
     * The class must make sure that it stays in sync with outView.
     * If lease is not used, it is simply null.
     */
    private ArrayList<Integer> outViewDates = null;

    /**
     * Contains creation dates of elements in inView, if leasing is used.
     * The class must make sure that it stays in sync with inView.
     * If lease is not used, it is simply null.
     */
    private ArrayList<Integer> inViewDates = null;

    private ArrayList<Node> inView;

    /**
     * to support the lease mechanism. with randomised resubmission it would
     * not be necessary.
     */
    private int birthDate;

    public ScampHandshake(String n) {

        ScampHandshake.c = Configuration.getInt(n + "." + PAR_C, 0);
        ScampHandshake.indirTTL = Configuration.getInt(n+"."+PAR_INDIRTTL,-1);
        ScampHandshake.leaseTimeout = Configuration.getInt(n+"."+PAR_LEASE,-1);
        outView = new ArrayList<Node>();
        inView = new ArrayList<Node>();
        if(ScampHandshake.leaseTimeout>0)
        {
            outViewDates = new ArrayList<Integer>();
            inViewDates = new ArrayList<Integer>();
        }
        birthDate = CDState.getCycle();
    }

// ---------------------------------------------------------------------

    /**
     * The birthDate field will denote the birth of the clone, and not the
     * original birthDate.
     */
    public Object clone() {

        ScampHandshake scamp = null;
        try { scamp=(ScampHandshake) super.clone(); }
        catch( CloneNotSupportedException e ) {} // never happens
        scamp.outView = (ArrayList)outView.clone();
        scamp.inView = (ArrayList)inView.clone();
        if( outViewDates != null )
            scamp.outViewDates = (ArrayList)outViewDates.clone();
        if( inViewDates != null )
            scamp.inViewDates = (ArrayList)inViewDates.clone();
        scamp.birthDate = CDState.getCycle();
        return scamp;
    }

    // ======================================================================
    // P U B L I C  I N T E R F A C E
    // ======================================================================

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    @Override
    public int degree() {
        return 0;
    }

    @Override
    public Node getNeighbor(int i) {
        return null;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        return false;
    }

    @Override
    public boolean contains(Node neighbor) {
        return false;
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    @Override
    public List<Node> getPeers() {
        return null;
    }
}
