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
	 *            the peer which initiates the periodic protocol
	 * @param called
	 *            the peer that is called
	 * @param message
	 *            the message to send to the random neighbor
	 * 
	 * @return return a message
	 */
	public IMessage onPeriodicCall(Node origin, IMessage message);

	/**
	 * Join the network using the contact in argument.
	 * 
	 * @param contact
	 *            the peer that will introduce caller to the network
	 */
	public void join(Node contact);

	/**
	 * The event called when a peer join the network using us as contact peer.
	 * 
	 * @param origin
	 *            the subscriber
	 */
	public void onSubscription(Node origin);

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
	 * Clone
	 * 
	 * @return a clone of the instance calling it
	 */
	public IRandomPeerSampling clone();

}
