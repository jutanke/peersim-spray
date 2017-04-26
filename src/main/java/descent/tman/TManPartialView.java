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

		Node result = null;
		for (int i = 0; i < random; ++i) {
			result = it.next();
		}
		return result;
	}

	List<Node> getSample(final TMan other, List<Node> randomPeers, double size) {
		ArrayList<Node> rank = new ArrayList<Node>(this);
		rank.addAll(randomPeers);

		Comparator<Node> ranking = new Comparator<Node>() {

			public int compare(Node o1, Node o2) {
				IDescriptor o1Desc = ((TMan) o1.getProtocol(TMan.pid)).descriptor;
				IDescriptor o2Desc = ((TMan) o2.getProtocol(TMan.pid)).descriptor;

				if (other.descriptor.ranking(o1Desc) < other.descriptor.ranking(o2Desc)) {
					return -1;
				} else if (other.descriptor.ranking(o1Desc) > other.descriptor.ranking(o2Desc)) {
					return 1;
				} else {
					return 0;
				}
			}

		};

		Collections.sort(rank, ranking);

		return rank.subList(0, (int) size);
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

		List<Node> toKeep = rank.subList(0, size);
		List<Node> toThrow = rank.subList(size, rank.size());

		for (int i = 0; i < toKeep.size(); ++i) {
			this.add(toKeep.get(i));
		}

		for (int i = 0; i < toThrow.size(); ++i) {
			this.remove(toThrow.get(i));
		}
	}

}
