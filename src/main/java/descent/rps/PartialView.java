package descent.rps;

import java.util.ArrayList;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;

/**
 * Implementation of a basic partial view.
 */
public class PartialView implements IPartialView {

	public ArrayList<Node> partialView;

	public PartialView() {
		this.partialView = new ArrayList<Node>();
	}

	public List<Node> getPeers() {
		return (List<Node>) this.partialView.clone();
	}

	public List<Node> getPeers(int k) {
		ArrayList<Node> sample = new ArrayList<Node>();
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

	public boolean removeNode(Node peer) {
		int index = this.getIndex(peer);
		if (index >= 0) {
			this.partialView.remove(index);
		}
		return index >= 0;
	}

	public boolean addNeighbor(Node peer) {
		boolean isContaining = this.contains(peer);
		if (!isContaining) {
			this.partialView.add(peer);
		}
		return !isContaining;
	}

	public boolean contains(Node peer) {
		return this.getIndex(peer) >= 0;
	}

	public int size() {
		return this.partialView.size();
	}

	public void clear() {
		this.partialView.clear();
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		PartialView pv = new PartialView();
		pv.partialView = new ArrayList<Node>(this.partialView);
		return pv;
	}
}
