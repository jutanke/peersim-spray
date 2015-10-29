package descent.rps;

import java.util.List;

import peersim.core.Node;

/**
 * Basic functions composing the random peer sampling protocol.
 */
public interface IRandomPeerSampling {

    /**
     * Get the total outbound cost of tick rounds
     *
     * @return the outbound cost of the peer. The Value indicates how much "Data" was
     * sent from this peer (the metric simply adds up the number of peers that were sent
     * for exchange with other nodes)
     */
     // int[] generatedPeerSamplingCost();

    /**
     * Function called every delta time. It generally corresponds to a protocol
     * aiming to renew connections to handle churn. Often, a neighbor is chosen
     * to perform the operation.
     */
    void periodicCall();

    /**
     * The event trigger when the neighbor received a message from the periodic
     * call of a peer (cf periodicCall function).
     *
     * @param origin  the peer which initiates the periodic protocol
     * @param message the message to send to the random neighbor
     * @return return a message
     */
    IMessage onPeriodicCall(Node origin, IMessage message);

    /**
     * Join the network using the contact in argument.
     *
     * @param joiner  the peer that joins the network
     * @param contact the peer that will introduce caller to the network
     */
    void join(Node joiner, Node contact);

    /**
     * The event called when a peer join the network using us as contact peer.
     *
     * @param origin the subscriber
     */
    void onSubscription(Node origin);

    /**
     * Leave the network. Either does nothing, or may help the network to
     * recover
     */
    void leave();

    /**
     * Getter of the neighbors, it includes dead links too
     *
     * @param k the number of requested neighbors
     * @return a list of neighbors of size k, or size of the neighborhood if k
     * is too large
     */
    List<Node> getPeers(int k);

    /**
     * Getter of the neighbors, does not include peers that are dead
     *
     * @return a list of nodes
     */
    List<Node> getAliveNeighbors();

    /**
     * Clone
     *
     * @return a clone of the instance calling it
     */
    IRandomPeerSampling clone();

}
