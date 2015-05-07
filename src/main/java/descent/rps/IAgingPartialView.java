package descent.rps;

import java.util.List;

import peersim.core.Node;

/**
 * Functions related to the aging partial views used within Cyclon or Spray.
 * Elements inside this partial view are aging out.
 */
public interface IAgingPartialView {

	/**
	 * Increment the age of every elements in the partial view
	 */
	public void incrementAge();

	/**
	 * Getter of the oldest element in the partial view
	 * 
	 * @return the oldest peer, null if the partial view is empty
	 */
	public Node getOldest();

	/**
	 * Getter of the neighbors
	 * 
	 * @param caller
	 *            the identity of the peer calling this function
	 * @param neighbor
	 *            the chosen neighbor to exchange with
	 * @param isInitiator
	 *            define if the caller is the initiator of the exchange
	 * @return a list of neighbors being the sample to send to the chosen
	 *         neighbor
	 */
	public List<Node> getSample(Node caller, Node neighbor, boolean isInitiator);

	/**
	 * Remove the peer from the neighborhood, if multiples occurrences of the
	 * pair <neighbor, age> exist, only one occurrence is removed
	 * 
	 * @param peer
	 *            the peer to remove
	 * @param age
	 *            the age of the peer to remove
	 * @return true if the peer has been remove, false otherwise
	 */
	public boolean removeNode(Node peer, Integer age);

	/**
	 * Merge the sample with the partial view taking into account the old sent
	 * sample
	 * 
	 * @param me
	 *            the peer that calls the merge function
	 * @param other
	 *            the other peer that sent a message to the "me" peer
	 * @param newSample
	 *            the new received sample
	 * @param oldSample
	 *            the old sent sample
	 * @param isInitiator
	 *            define if the peer "me" is the initiator of the exchange
	 */
	public void mergeSample(Node me, Node other, List<Node> newSample,
			List<Node> oldSample, boolean isInitiator);

}
