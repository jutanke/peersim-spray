package descent.rps;

import java.util.List;

import peersim.core.Node;

/**
 * Basic functions composing the random peer sampling protocol.
 */
public interface IRandomPeerSampling {

	/**
	 * Function called every delta time. It generally corresponds to a protocol
	 * aiming to renew connections to handle churn. Often, a neighbor is chosen
	 * to perform the operation.
	 */
	public void periodicCall();

	/**
	 * The event trigger when the neighbor received a message from the periodic
	 * call of a peer (cf periodicCall function).
	 * 
	 * @param origin
	 * 
	 * @return return a list of peers
	 */
	public List<Node> onPeriodicCall(IRandomPeerSampling origin,
			IMessage message);

	/**
	 * Join the network using the contact in argument.
	 * 
	 * @param contact
	 *            the contact inside the network which will introduce us
	 */
	public void join(IRandomPeerSampling contact);

	/**
	 * The event called when a peer join the network using us as contact peer.
	 * 
	 * @param joiner
	 *            the peer that joins the network
	 */
	public void onSubscription(IRandomPeerSampling joiner);

	/**
	 * Leave the network. Either does nothing, or may help the network to
	 * recover
	 */
	public void leave();

	/**
	 * Getter of the neighbors
	 * 
	 * @param k
	 *            the number of requested neighbors
	 * 
	 * @return a list of neighbors of size k, or size of the neighborhood if k
	 *         is too large
	 */
	public List<Node> getPeers(int k);

	/**
	 * Tries to add the neighbor to the partial view
	 * 
	 * @return true if the neighbor has been added, false otherwise
	 */
	public boolean addNeighbor(Node peer);

	/**
	 * Clone
	 * 
	 * @return a clone of the instance calling it
	 */
	public IRandomPeerSampling clone();

}
