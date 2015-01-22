package example.webrtc;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Node;
import peersim.core.Protocol;

/**
 * Created by julian on 22/01/15.
 */
public class WebRtcNode implements Node {

    // ================= fields ========================================
    // =================================================================
    /**
     * used to generate unique IDs
     */
    private static long counterID = -1;

    /**
     * The protocols on this node.
     */
    protected Protocol[] protocol = null;

    /**
     * The current index of this node in the node
     * list of the {@link Network}. It can change any time.
     * This is necessary to allow
     * the implementation of efficient graph algorithms.
     */
    private int index;

    /**
     * The fail state of the node.
     */
    protected int failstate = Fallible.OK;

    /**
     * The ID of the node. It should be final, however it can't be final because
     * clone must be able to set it.
     */
    private long ID;

    // ================ constructor and initialization =================
    // =================================================================

    public WebRtcNode() {
        String[] names = Configuration.getNames(PAR_PROT);
        CommonState.setNode(this);
        this.protocol = new Protocol[names.length];
        for (int i = 0; i < names.length; i++) {
            CommonState.setPid(i);
            protocol[i] = (Protocol) Configuration.getInstance(names[i]);
        }
    }


    // -----------------------------------------------------------------

    @Override
    public Object clone() {
        WebRtcNode result = null;
        try { result=(WebRtcNode)super.clone(); }
        catch( CloneNotSupportedException e ) {} // never happens
        result.protocol = new Protocol[protocol.length];
        CommonState.setNode(result);
        result.ID=nextID();
        for(int i=0; i<protocol.length; ++i) {
            CommonState.setPid(i);
            result.protocol[i] = (Protocol)protocol[i].clone();
        }
        return result;
    }

    // -----------------------------------------------------------------

    /** returns the next unique ID */
    private long nextID() {

        return counterID++;
    }

    // =============== public methods ==================================
    // =================================================================

    @Override
    public Protocol getProtocol(int i) {
        return null;
    }

    @Override
    public int protocolSize() {
        return 0;
    }

    @Override
    public void setIndex(int index) {

    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public long getID() {
        return 0;
    }

    @Override
    public int getFailState() {
        return 0;
    }

    @Override
    public void setFailState(int failState) {

    }

    @Override
    public boolean isUp() {
        return false;
    }


}
