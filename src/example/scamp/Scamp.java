package example.scamp;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * CODE TAKEN FROM: https://github.com/csko/Peersim/tree/master/scamp
 *
 * Created by julian on 28/01/15.
 */
public class Scamp implements CDProtocol, Linkable {

    // =================== static fields ==================================
    // ====================================================================


    /**
     * Parameter "c" of Scamp . Defaults to 0.
     *
     * @config
     */
    private static final String PAR_C = "c";

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

    /**
     * c
     */
    private static int c;

    /**
     * indirection TTL
     */
    private static int indirTTL;

    /**
     * lease timeout
     */
    private static int leaseTimeout;

    // =================== fields =========================================
    // ====================================================================

    /** contains Nodes */
    private ArrayList<Node> outView = null;

    /**
     * Contains creation dates of elements in outView, if leasing is used.
     * The class must make sure that it stays in sync with outView.
     * If lease is not used, it is simply null.
     */
    private ArrayList<Integer> outViewDates = null;

    /** contains Nodes */
    private ArrayList<Node> inView = null;

    /**
     * Contains creation dates of elements in inView, if leasing is used.
     * The class must make sure that it stays in sync with inView.
     * If lease is not used, it is simply null.
     */
    private ArrayList<Integer> inViewDates = null;

    /**
     * to support the lease mechanism. with randomised resubmission it would
     * not be necessary.
     */
    private int birthDate;

    // ===================== initialization ================================
    // =====================================================================


    public Scamp(String n) {

        Scamp.c = Configuration.getInt(n + "." + PAR_C, 0);
        Scamp.indirTTL = Configuration.getInt(n+"."+PAR_INDIRTTL,-1);
        Scamp.leaseTimeout = Configuration.getInt(n+"."+PAR_LEASE,-1);
        outView = new ArrayList<Node>();
        inView = new ArrayList<Node>();
        if(Scamp.leaseTimeout>0)
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

        Scamp scamp = null;
        try { scamp=(Scamp) super.clone(); }
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

    // ====================== The core Scamp protocols =======================
    // =======================================================================


    // ====================== Linkable implementation =====================
    // ====================================================================

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

    // ===================== CDProtocol implementations ===================
    // ====================================================================

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    // ===================== Other ========================================
// ====================================================================

    /** Method to check the consistency of the state of the network */
    public static String test(int protocolID) {

        int failOutLinks=0; // out link points to failed node
        int failInLinks=0; // in link points to failed node
        int corruptDates=-1; // outViewDate is wrong size or not ordered
        int corruptInDates=-1; // inViewDate is wrong size or not ordered
        int missingInLinks=0; // no corresponding in view link
        int missingOutLinks=0; // no corresponding out view link

        if( Scamp.leaseTimeout >= 0 ) corruptInDates=corruptDates = 0 ;

        for(int i=0; i< Network.size(); ++i)
        {
            Node curr = Network.get(i);
            Scamp currsc = (Scamp)(curr.getProtocol(protocolID));

            // check out view
            for(int j=0; j<currsc.degree(); ++j)
            {
                Node out = currsc.outView.get(j);
                if(!out.isUp())
                {
                    ++failOutLinks;
                    ++missingInLinks;
                }
                else
                {
                    Scamp outsc=(Scamp)out.getProtocol(protocolID);
                    if(!outsc.inView.contains(curr))
                        ++missingInLinks;
                }
            }

            // check in view
            for(int j=0; j<currsc.inView.size(); ++j)
            {
                Node in = currsc.inView.get(j);
                if(!in.isUp())
                {
                    ++failInLinks;
                    ++missingOutLinks;
                }
                else
                {
                    Scamp insc=(Scamp)in.getProtocol(protocolID);
                    if(!insc.outView.contains(curr))
                        ++missingOutLinks;
                }
            }

            // check dates if any
            if( currsc.outViewDates == null ) continue;
            if( currsc.outViewDates.size() != currsc.degree() )
                corruptDates++;
            else if ( currsc.outViewDates.size() > 0 )
            {
                for(int j=1; j<currsc.degree(); ++j)
                {
                    if( currsc.outViewDates.get(j-1)
                            .compareTo(currsc.outViewDates.get(j)) > 0 )
                    {
                        corruptDates++;
                        break;
                    }
                }

            }
            if( currsc.inViewDates.size() != currsc.inView.size() )
                corruptInDates++;
            else if ( currsc.inViewDates.size() > 0 )
            {
                for(int j=1; j<currsc.inView.size(); ++j)
                {
                    if( currsc.inViewDates.get(j-1).compareTo(
                            currsc.inViewDates.get(j)) > 0 )
                    {
                        corruptInDates++;
                        break;
                    }
                }

            }
        }

        return ("failOutLinks="+failOutLinks+
                " failInLinks="+failInLinks+
                " missingOutLinks="+missingOutLinks+
                " missingInLinks="+missingInLinks+
                " corruptDates="+corruptDates+
                " corruptInDates="+corruptInDates);
    }
}
