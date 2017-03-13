package descent.spray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import descent.rps.AAgingPartialView;
import peersim.core.CommonState;
import peersim.core.Node;

/**
 * Aging partial view of the Spray random peer sampling protocol. It can contain
 * multiple references of a same peer.
 */
public class SprayPartialView extends AAgingPartialView {

	/**
	 * Constructor of the class
	 */
	public SprayPartialView() {
		super();
	}

	public List<Node> getSample(Node caller, Node neighbor, boolean isInitiator) {
		ArrayList<Node> sample = new ArrayList<Node>();
		ArrayList<Node> clone = new ArrayList<Node>(this.partialView);

		// #A if the caller in the initiator, it automatically adds itself

		int sampleSize = (int) Math.ceil(clone.size() / 2.0);
		if (isInitiator) { // called from the chosen peer
			clone.remove(0);// replace an occurrence of the chosen neighbor
			sample.add(caller); // by the initiator identity
		}

		// #B create the sample from random peers inside the partial view
		while (sample.size() < sampleSize) {
			int rn = CommonState.r.nextInt(clone.size());
			sample.add(clone.get(rn));
			clone.remove(rn);
		}

		// #C since the partial view can contain multiple references to a
		// neighbor, including the chosen peer to exchange with, we replace
		// them with references of the caller
		sample = (ArrayList<Node>) replace(sample, neighbor, caller);

		return sample;
	}

	/**
	 * Replace all the occurrences of the old node by the fresh one in the
	 * sample in argument
	 * 
	 * @param sample
	 *            the list containing the elements to replace
	 * @param old
	 *            the peer to replace
	 * @param fresh
	 *            the peer to insert
	 * @return a new list of node with replaced elements
	 */
	private static List<Node> replace(List<Node> sample, Node old, Node fresh) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (int i = 0; i < sample.size(); ++i) {
			if (sample.get(i).getID() == old.getID()) {
				result.add(fresh);
			} else {
				result.add(sample.get(i));
			}
		}
		return result;
	}

	public void mergeSample(Node caller, Node neighbor, List<Node> newSample, List<Node> oldSample,
			boolean isInitiator) {
		ArrayList<Node> oldSampleInitial = (ArrayList<Node>) replace(oldSample, caller, neighbor); // opposite
																									// transformation
																									// of
																									// the
																									// getSample

		// #A remove the original sample
		for (Node toRemoveNeighbor : oldSampleInitial) {
			this.removeNode(toRemoveNeighbor);
		}

		// #B add the received sample
		for (Node toAddNeighbor : newSample) {
			boolean found = false;
			int i = 0;
			if (this.partialView.contains(toAddNeighbor)) {
				// #1 search for a removed peer that is not duplicate
				while (!found && oldSampleInitial.size() > i) {
					if (!this.partialView.contains(oldSampleInitial.get(i))) {
						found = true;
					} else {
						++i;
					}
				}
			}
			if (!found) {
				this.addNeighbor(toAddNeighbor);
			} else {
				this.addNeighbor(oldSampleInitial.get(i));
			}
		}
	}

	public boolean addNeighbor(Node peer) {
		// we do not check for doubles since Spray allows them
		this.partialView.add(peer);
		this.ages.add(new Integer(0));
		return true;
	}

	/**
	 * Remove all occurrences of the neighbor and count them
	 * 
	 * @param neighbor
	 *            the neighbor to remove from the neighborhood
	 * @return the number of removals
	 */
	public int removeAll(Node neighbor) {
		int occ = 0;
		int i = 0;
		while (i < this.partialView.size()) {
			if (this.partialView.get(i).getID() == neighbor.getID()) {
				this.partialView.remove(i);
				this.ages.remove(i);
				++occ;
			} else {
				++i;
			}
		}
		return occ;
	}

	public Integer count(Node nodeToCount) {
		Integer result = 0;
		for (Node node : this.partialView) {
			if (nodeToCount.equals(node)) {
				++result;
			}
		}
		return result;
	}

	public Node getLowestOcc() {
		// ugly but whatever
		HashMap<Node, Integer> occurences = new HashMap<Node, Integer>();
		for (Node n : this.partialView) {
			if (n.isUp()) {
				if (!occurences.containsKey(n)) {
					occurences.put(n, 0);
				}
				occurences.put(n, occurences.get(n) + 1);
			}
		}
		Integer min = Integer.MAX_VALUE;
		Node result = null;
		for (Node k : occurences.keySet()) {
			if (occurences.get(k) < min) {
				min = occurences.get(k);
				result = k;
			}
		}
		return result;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SprayPartialView spv = new SprayPartialView();
		spv.partialView = new ArrayList<Node>(this.partialView);
		spv.ages = new ArrayList<Integer>(this.ages);
		return spv;
	}
}
