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
	 * Getter of the neighbors list
	 * 
	 * @return the list to the neighborhood
	 */
	public List<Node> getPeers();

	/**
	 * Get a list of k neighbors. If the partial view is not big enough, it
	 * returns the partial view
	 * 
	 * @param k
	 *            the number of requested neighbors
	 * @return a list of nodes
	 */
	public List<Node> getPeers(int k);

	/**
	 * Getter of the neighbors
	 * 
	 * @param neighbor
	 *            the chosen neighbor to exchange with
	 * 
	 * @return a list of neighbors being the sample to send to the chosen
	 *         neighbor
	 */
	public List<Node> getSample(Node neighbor);

	/**
	 * Remove the peer from the neighborhood, if multiple occurences of the peer
	 * exist, it remove the oldest
	 * 
	 * @param peer
	 *            the peer to remove
	 * @return true if the peer has been removed, false otherwise
	 */
	public boolean removeNode(Node peer);

	/**
	 * Remove the peer from the neighborhood
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
	 * @param neighbor
	 *            the neighbor chosen for the exchange
	 * @param newSample
	 *            the new received sample
	 * @param oldSample
	 *            the old sent sample
	 */
	public void mergeSample(Node neighbor, List<Node> newSample,
			List<Node> oldSample);

	/**
	 * Check if the partial view contains a peer
	 * 
	 * @param peer
	 *            the peer to search
	 * @return true if the partial view contains the peer, false otherwise
	 */
	public boolean contains(Node peer);

	/**
	 * Getter of the size of the partial view
	 * 
	 * @return the size of the partial view
	 */
	public int size();
}
