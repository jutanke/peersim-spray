package descent.rps;

import java.util.ArrayList;
import java.util.List;

import descent.cyclon.CyclonPartialView;
import peersim.core.CommonState;
import peersim.core.Node;

public abstract class AAgingPartialView implements IAgingPartialView {

	protected ArrayList<Node> partialView;
	protected ArrayList<Integer> ages;

	public AAgingPartialView() {
		this.partialView = new ArrayList<Node>();
		this.ages = new ArrayList<Integer>();
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
		if (this.partialView.size() == k || k == Integer.MAX_VALUE) {
			sample = new ArrayList<Node>(this.partialView);
		} else {
			sample = new ArrayList<Node>();
			ArrayList<Node> clone = new ArrayList<Node>(this.partialView);
			while (sample.size() < Math.min(k, this.partialView.size())) {
				int rn = CommonState.r.nextInt(clone.size());
				sample.add(clone.get(rn));
				clone.remove(rn);
			}
		}
		return sample;
	}

	public abstract List<Node> getSample(Node caller, Node neighbor,
			boolean isInitiator);

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
		while (!found && i < this.partialView.size() && this.ages.get(i) >= age) {
			if (this.partialView.get(i).getID() == peer.getID()) {
				found = true;
			} else {
				++i;
			}
		}
		if (found) {
			this.partialView.remove(i);
			this.ages.remove(i);
		}
		return found;
	}

	public abstract void mergeSample(Node me, Node other, List<Node> newSample,
			List<Node> oldSample, boolean isInitiator);

	public abstract boolean addNeighbor(Node peer);

	public boolean contains(Node peer) {
		return this.getIndex(peer) >= 0;
	}

	public int size() {
		return this.partialView.size();
	}

	public void clear() {
		this.partialView.clear();
		this.ages.clear();
	}

	public int getIndex(Node neighbor) {
		int i = 0;
		int index = -1;
		boolean found = false;
		while (!found && i < this.partialView.size()) {
			if (this.partialView.get(i).getID() == neighbor.getID()) {
				found = true;
				index = i;
			}
			++i;
		}
		return index;
	}

}
