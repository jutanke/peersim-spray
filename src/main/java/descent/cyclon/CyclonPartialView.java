package descent.cyclon;

import java.util.ArrayList;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;
import descent.rps.AAgingPartialView;

/**
 * Aging partial view class used within Cyclon random peer sampling protocol
 */
public class CyclonPartialView extends AAgingPartialView {

	// #A bounds
	private static int l;
	private static int c;

	/**
	 * Constructor of the Cyclon's partial view
	 * 
	 * @param c
	 *            the maximum size of the partial view
	 * @param l
	 *            the maximum size of the samples
	 */
	public CyclonPartialView(int c, int l) {
		super();
		CyclonPartialView.l = l;
		CyclonPartialView.c = c;
	}

	@Override
	public List<Node> getSample(Node caller, Node neighbor, boolean isInitiator) {
		ArrayList<Node> sample = new ArrayList<Node>();
		ArrayList<Node> clone = new ArrayList<Node>(this.partialView);

		int sampleSize = clone.size();
		if (!isInitiator) { // called from the chosen peer
			sampleSize = Math.min(sampleSize, CyclonPartialView.l);
		} else { // called from the initiating peer
			sampleSize = Math.min(sampleSize - 1, CyclonPartialView.l - 1);
			sampleSize = Math.max(sampleSize, 0);
			clone.remove(0);
			sample.add(caller);
		}

		while (sample.size() < sampleSize) {
			int rn = CommonState.r.nextInt(clone.size());
			sample.add(clone.get(rn));
			clone.remove(rn);
		}
		return sample;
	}

	@Override
	public void mergeSample(Node me, Node other, List<Node> newSample,
			List<Node> oldSample, boolean isInitiator) {
		ArrayList<Node> removedPeer = new ArrayList<Node>();
		ArrayList<Integer> removedAge = new ArrayList<Integer>();

		// #1 remove the sent sample
		for (Node old : oldSample) {
			int index = this.getIndex(old);
			if (index >= 0) {
				removedPeer.add(this.partialView.get(index));
				removedAge.add(this.ages.get(index));
				this.partialView.remove(index);
				this.ages.remove(index);
			}
		}

		// #2 remove the chosen neighbor
		this.removeNode(other);

		// #3 insert the new sample
		for (Node fresh : newSample) {
			if (!this.contains(fresh) && fresh.getID() != me.getID()) {
				this.partialView.add(fresh);
				// #A look into the removing if it existed
				boolean found = false;
				int i = 0;
				while (!found && i < removedPeer.size()) {
					if (removedPeer.get(i).getID() == fresh.getID()) {
						found = true;
					} else {
						++i;
					}
				}
				// #B if it existed, keep the old age
				if (found) {
					this.ages.add((Integer) removedAge.get(i));
				} else {
					// #C otherwise, it's a brand new one
					this.ages.add(new Integer(0));
				}
			}
		}

		// #4 fill with old elements until the maximum size is reached
		int i = removedPeer.size() - 1;
		while (i >= 0 && this.partialView.size() < CyclonPartialView.c) {
			if (!this.contains(removedPeer.get(i))) {
				// #A search the insert position
				int position = this.ages.size() - 1;
				boolean found = false;
				while (!found && position >= 0) {
					if (this.ages.get(position) >= removedAge.get(i)) {
						found = true;
					} else {
						--position;
					}
				}
				// #B insert at the rightful position to maintain the order
				if (!found) {
					this.partialView.add(0, removedPeer.get(i));
					this.ages.add(0, removedAge.get(i));
				} else {
					this.ages.add(position + 1, removedAge.get(i));
					this.partialView.add(position + 1, removedPeer.get(i));
				}

			}
			--i;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		CyclonPartialView cpv = new CyclonPartialView(CyclonPartialView.c,
				CyclonPartialView.l);
		cpv.partialView = new ArrayList<Node>(this.partialView);
		cpv.ages = new ArrayList<Integer>(this.ages);
		return cpv;
	}

	@Override
	public boolean addNeighbor(Node peer) {
		boolean isContaining = this.contains(peer);
		if (!isContaining) {
			this.partialView.add(peer);
			this.ages.add(new Integer(0));
		}
		return !isContaining;
	}

}
