package example.webrtc;

import peersim.core.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This is supposed to wrap the webrtc component around the node.
 * In WebRtc we cannot just address a node, instead, we must handshake first
 *
 * Created by julian on 23/01/15.
 */
public class WebRtcWrapper {

    public final Node node;
    private Stack<WebRtcWrapper> pendingHandshakes;
    private Map<Long, WebRtcWrapper> openConnections;

    public WebRtcWrapper(Node n) {
        this.node = n;
        this.pendingHandshakes = new Stack<WebRtcWrapper>();
        this.openConnections = new HashMap<Long, WebRtcWrapper>();
    }

    // =============== public interfaces ===================================
    // =====================================================================

    /**
     * This function simulates the handshake. The return of the protocol
     * will be delayed for one cycle!
     * @param other
     */
    public void offer(WebRtcWrapper other) {
        this.pendingHandshakes.push(other);
    }

    /**
     * A nodes replay to an offer
     * @param other
     */
    public void answer(WebRtcWrapper other) {
        this.openConnections.put(other.node.getID(), other);
    }

    /**
     * this is supposed to be called from the update function
     */
    public void tick() {
        if (this.pendingHandshakes.size() > 0) {
            WebRtcWrapper w = this.pendingHandshakes.pop();
            this.openConnections.put(w.node.getID(), w);
        }
    }
}
