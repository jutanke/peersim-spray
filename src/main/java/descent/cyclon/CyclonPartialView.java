package descent.cyclon;

import java.util.ArrayList;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;
import descent.rps.IAgingPartialView;

/**
 * Aging partial view class used within Cyclon random peer sampling protocol
 */
public class CyclonPartialView implements IAgingPartialView {

	// #A local structures. Overall it behaves as an ordered set
	private ArrayList<Node> partialView;
	private ArrayList<Integer> ages;

	// #B bounds
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
		CyclonPartialView.l = l;
		CyclonPartialView.c = c;
		this.partialView = new ArrayList<Node>(CyclonPartialView.c);
		this.ages = new ArrayList<Integer>(CyclonPartialView.c);
	}

	public void incrementAge() {
		for (Integer age : this.ages) {
			++age;
		}
	}

	public Node getOldest() {
		return this.partialView.get(0);
	}

	public List<Node> getPeers() {
		return this.partialView;
	}

	public List<Node> getPeers(int k) {
		ArrayList<Node> sample;
		if (this.partialView.size() == k) {
			sample = new ArrayList<Node>(this.partialView);
		} else {
			sample = new ArrayList<Node>(CyclonPartialView.l);
			ArrayList<Node> clone = new ArrayList<Node>(this.partialView);
			while (sample.size() < Math.min(k, this.partialView.size())) {
				int rn = CommonState.r.nextInt(clone.size());
				sample.add(clone.get(rn));
				clone.remove(rn);
			}
		}
		return sample;
	}

	public List<Node> getSample(Node neighbor) {
		ArrayList<Node> sample = new ArrayList<Node>(CyclonPartialView.l);
		ArrayList<Node> clone = new ArrayList<Node>(this.partialView);
		while (sample.size() < Math.min(CyclonPartialView.l,
				this.partialView.size() - 1)) {
			int rn = CommonState.r.nextInt(clone.size());
			if (clone.get(rn).getID() != neighbor.getID()) {
				sample.add(clone.get(rn));
			}
			clone.remove(rn);
		}
		return sample;
	}

	public boolean removeNode(Node peer) {
		int index = this.getIndex(peer);
		if (index >= 0) {
			this.partialView.remove(index);
			this.ages.remove(index);
		}
		return index >= 0;
	}

	public boolean removeNode(Node peer, Integer age) {
		int i = 0;
		boolean found = false;
		while (i < this.partialView.size() && !found) {
			if (this.partialView.get(i).getID() == peer.getID()
					&& this.ages.get(i) == age) {
				found = true;
				this.partialView.remove(i);
				this.ages.remove(i);
			}
			++i;
		}
		return found;
	}

	public boolean contains(Node peer) {
		return this.getIndex(peer) >= 0;
	}

	public void mergeSample(Node neighbor, List<Node> newSample,
			List<Node> oldSample) {
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
		this.partialView.remove(neighbor);

		// #3 insert the new sample
		for (Node fresh : newSample) {
			if (!this.contains(fresh)) {
				this.partialView.add(fresh);
				this.ages.add(new Integer(0));
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
					if (this.ages.get(position) >= removedAge.get(position)) {
						found = true;
					} else {
						--position;
					}
				}
				// #B insert at the rightful position to maintain the order
				this.partialView.add(position + 1, removedPeer.get(i));
				this.ages.add(position + 1, removedAge.get(i));
			}
			--i;
		}
	}

	/**
	 * Getter of the index of the peer in the partial view
	 * 
	 * @param peer
	 *            the peer to search
	 * @return the index in the partial view, -1 if not found
	 */
	private int getIndex(Node peer) {
		int index = -1;
		int i = 0;
		boolean found = false;
		while (!found && i < this.partialView.size()) {
			if (this.partialView.get(i).getID() == peer.getID()) {
				found = true;
				index = i;
			}
			++i;
		}
		return index;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		CyclonPartialView cpv = new CyclonPartialView(CyclonPartialView.c,
				CyclonPartialView.l);
		cpv.partialView = new ArrayList<Node>(this.partialView);
		cpv.ages = new ArrayList<Integer>(this.ages);
		return cpv;
	}

	public int size() {
		return this.partialView.size();
	}
}
