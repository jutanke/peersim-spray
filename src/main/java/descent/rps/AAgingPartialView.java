package descent.rps;

import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;

public abstract class AAgingPartialView extends PartialView implements
		IAgingPartialView {

	protected ArrayList<Integer> ages;

	public AAgingPartialView() {
		super();
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

	public abstract List<Node> getSample(Node caller, Node neighbor,
			boolean isInitiator);

	@Override
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

	@Override
	public abstract boolean addNeighbor(Node peer);

	public void clear() {
		super.clear();
		this.ages.clear();
	}

}
