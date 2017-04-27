package descent.tman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;

public class TManPartialView extends HashSet<Node> {

	/**
	 * Get a random peer from the map
	 * 
	 * @return Node
	 */
	public Node getRandom() {
		Integer size = this.size();
		Iterator<Node> it = this.iterator();
		Integer random = CommonState.r.nextInt(size);

		Node result = it.next();
		for (int i = 0; i < random; ++i) {
			result = it.next();
		}
		return result;
	}

	List<Node> getSample(Node caller, Node other, List<Node> randomPeers, double size) {
		final TMan callerTMan = ((TMan) caller.getProtocol(TMan.pid));
		final TMan otherTMan = ((TMan) other.getProtocol(TMan.pid));

		ArrayList<Node> rank = new ArrayList<Node>();
		for (Node n : this) {
			if (n.equals(other)) {
				rank.add(caller);
			} else {
				rank.add(n);
			}
		}

		for (Node n : randomPeers) {
			Node toAdd = n;
			if (toAdd.equals(other)) {
				toAdd = caller;
			}
			if (!rank.contains(toAdd)) {
				rank.add(toAdd);
			}
		}

		// ArrayList<Node> rank = new ArrayList<Node>(this);
		rank.addAll(randomPeers);

		Comparator<Node> ranking = new Comparator<Node>() {

			public int compare(Node o1, Node o2) {
				IDescriptor o1Desc = ((TMan) o1.getProtocol(TMan.pid)).descriptor;
				IDescriptor o2Desc = ((TMan) o2.getProtocol(TMan.pid)).descriptor;

				if (otherTMan.descriptor.ranking(o1Desc) < otherTMan.descriptor.ranking(o2Desc)) {
					return -1;
				} else if (otherTMan.descriptor.ranking(o1Desc) > otherTMan.descriptor.ranking(o2Desc)) {
					return 1;
				} else {
					return 0;
				}
			}

		};

		Collections.sort(rank, ranking);

		return rank.subList(0, Math.min((int) size, rank.size()));
	}

	public void merge(final TMan myself, List<Node> sample, Integer size) {
		ArrayList<Node> rank = new ArrayList<Node>(this);

		for (int i = 0; i < sample.size(); ++i) {
			if (!rank.contains(sample.get(i))) {
				rank.add(sample.get(i));
			}
		}

		Comparator<Node> ranking = new Comparator<Node>() {

			public int compare(Node o1, Node o2) {
				IDescriptor o1Desc = ((TMan) o1.getProtocol(TMan.pid)).descriptor;
				IDescriptor o2Desc = ((TMan) o2.getProtocol(TMan.pid)).descriptor;

				if (myself.descriptor.ranking(o1Desc) < myself.descriptor.ranking(o2Desc)) {
					return -1;
				} else if (myself.descriptor.ranking(o1Desc) > myself.descriptor.ranking(o2Desc)) {
					return 1;
				} else {
					return 0;
				}
			}

		};

		Collections.sort(rank, ranking);

		List<Node> toKeep = rank.subList(0, Math.min(size, rank.size()));
		List<Node> toThrow = rank.subList(Math.min(size, rank.size()), rank.size());

		for (int i = 0; i < toKeep.size(); ++i) {
			this.add(toKeep.get(i));
		}

		for (int i = 0; i < toThrow.size(); ++i) {
			this.remove(toThrow.get(i));
		}
	}

}
