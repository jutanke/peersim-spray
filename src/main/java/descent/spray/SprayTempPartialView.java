package descent.spray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;
import descent.rps.AAgingPartialView;

/**
 * Aging partial view of the Spray random peer sampling protocol. It can contain
 * multiple references of a same peer.
 */
public class SprayTempPartialView extends AAgingPartialView {

	/**
	 * Constructor of the class
	 */
	public SprayTempPartialView() {
		super();
	}

	public List<Node> getSample(Node caller, Node neighbor, boolean isInitiator) {
		ArrayList<Node> sample = new ArrayList<Node>();
		ArrayList<Node> clone = new ArrayList<Node>(this.partialView);

		// #A if the caller in the initiator, it automatically adds itself
		int sampleSize = (int) Math.ceil(clone.size() / 2);
		if (isInitiator) { // called from the chosen peer
			--sampleSize;
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

	public void mergeSample(Node caller, Node neighbor, List<Node> newSample,
			List<Node> oldSample, boolean isInitiator) {
		ArrayList<Node> oldSampleInitial = (ArrayList<Node>) replace(oldSample,
				caller, neighbor); // opposite transformation of the getSample

		// #A remove the original sample
		for (Node toRemoveNeighbor : oldSampleInitial) {
			this.removeNode(toRemoveNeighbor);
		}

		// #B add the received sample
		for (Node toAddNeighbor : newSample) {
			this.addNeighbor(toAddNeighbor);
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

	@Override
	protected Object clone() throws CloneNotSupportedException {
		SprayTempPartialView spv = new SprayTempPartialView();
		spv.partialView = new ArrayList<Node>(this.partialView);
		spv.ages = new ArrayList<Integer>(this.ages);
		return spv;
	}
}
