package descent.spray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class MergingRegister {

	public LinkedList<HashSet<Integer>> networks;
	public HashSet<Integer> flattenNetworks;
	public Integer size;

	public HashSet<Integer> waiting;
	public HashMap<HashSet<Integer>, Integer> got; // NetworkId -> size

	public MergingRegister() {
		this.networks = new LinkedList<HashSet<Integer>>();
		this.flattenNetworks = new HashSet<Integer>();
		this.size = -1;

		this.waiting = new HashSet<Integer>();
		this.got = new HashMap<HashSet<Integer>, Integer>();
	}

	public MergingRegister(Integer networkId) {
		this.networks = new LinkedList<HashSet<Integer>>();
		this.flattenNetworks = new HashSet<Integer>();
		this.size = -1;

		this.waiting = new HashSet<Integer>();
		this.got = new HashMap<HashSet<Integer>, Integer>();

		this.initialize(networkId);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		MergingRegister clone = new MergingRegister();
		clone.networks = (LinkedList<HashSet<Integer>>) this.networks.clone();
		clone.flattenNetworks = (HashSet<Integer>) this.flattenNetworks.clone();
		clone.size = new Integer(this.size);
		clone.waiting = (HashSet<Integer>) this.waiting.clone();
		clone.got = (HashMap<HashSet<Integer>, Integer>) this.got.clone();
		return clone;
	}

	public void initialize(Integer networkId) {
		// #1 add the network to the ordered list
		HashSet<Integer> network = new HashSet<Integer>();
		network.add(networkId);
		this.networks.push(network);
		// #2 add it to the flat keys
		flattenNetworks.add(networkId);
	}

	public double isMerge(SprayMessage m, Integer currentSize) {
		double result = 0.0;
		this.newNetworkDetected(m, currentSize);
		if (this.keepSize(m)) {
			result = this.checkMerge(m, currentSize);
		}
		return result;
	}

	public double checkMerge(SprayMessage m, Integer currentSize) {
		if (this.waiting.isEmpty()) {
			return 0.0;
		}

		HashSet<HashSet<Integer>> permutation = this.permutations(
				(HashSet<Integer>) this.waiting.clone(),
				new HashSet<HashSet<Integer>>());
		//if (this.waiting.size() > 5 && this.got.keySet().size() > 5
		//		&& permutation == null) {
		//	System.out.println(this.toString());
		//}

		if (permutation == null) {
			return 0.0; // (TODO) un-uglify this kind of code
		}

		Integer localSize = currentSize;
		if (this.size != -1) {
			localSize = this.size;
		}
		Double networkSize = Math.exp(localSize);
		for (HashSet<Integer> network : permutation) {
			networkSize += Math.exp(this.got.get(network));
		}

		Double result = this.oneParameterFormula(Math.exp(localSize)
				/ networkSize);
		for (HashSet<Integer> network : permutation) {
			result += this.oneParameterFormula(Math.exp(this.got.get(network))
					/ networkSize);
		}
		this.flush();
		return result;
	}

	private Double oneParameterFormula(Double ratio) {
		return -ratio * Math.log(ratio);
	}

	public HashSet<HashSet<Integer>> permutations(HashSet<Integer> objective,
			HashSet<HashSet<Integer>> current) {
		if (objective.isEmpty()) {
			return current;
		}
		HashSet<HashSet<Integer>> result = null;
		Iterator<HashSet<Integer>> iNetworks = this.got.keySet().iterator();
		while (iNetworks.hasNext() && result == null) {
			HashSet<Integer> networkExamined = iNetworks.next();
			if (objective.containsAll(networkExamined)) {
				HashSet<Integer> cloneObjective = (HashSet<Integer>) objective
						.clone();
				cloneObjective.removeAll(networkExamined);
				HashSet<HashSet<Integer>> cloneCurrent = (HashSet<HashSet<Integer>>) current
						.clone();
				cloneCurrent.add(networkExamined);
				result = this.permutations(cloneObjective, cloneCurrent);
			}
		}
		return result;
	}

	public boolean keepSize(SprayMessage m) {
		// #1 handle the very first merge case
		HashSet<Integer> idNetworkSize = new HashSet<Integer>();
		if (m.networks.size() == 1) {
			idNetworkSize.addAll(m.networks.getFirst());
		} else {
			for (int i = 1; i < m.networks.size(); ++i) {
				idNetworkSize.addAll(m.networks.get(i));
			}
		}
		// #2 the size may be interesting for future merge, save it
		if (this.waiting.containsAll(idNetworkSize)
				&& !this.got.containsKey(idNetworkSize)) {
			this.got.put((HashSet<Integer>) idNetworkSize.clone(), m.size);
			return true;
		}
		return false;
	}

	public boolean newNetworkDetected(SprayMessage m, Integer currentSize) {
		// #1 detect if new networks exist
		// #A flatten the networks from the message
		HashSet<Integer> flattenNetworksMessage = new HashSet<Integer>();
		for (HashSet<Integer> network : m.networks) {
			for (Integer networkId : network) {
				flattenNetworksMessage.add(networkId);
			}
		}
		// #B process the difference
		flattenNetworksMessage.removeAll(this.flattenNetworks);
		// #C add the differences to the awaited network members
		if (this.waiting.isEmpty() && flattenNetworksMessage.size() > 0) {
			this.size = currentSize;
			this.networks.addFirst(new HashSet<Integer>());
		}
		if (this.waiting.addAll(flattenNetworksMessage)) {
			// #D add the networks identifiers to networks
			this.networks.getFirst().addAll(flattenNetworksMessage);
			this.flattenNetworks.addAll(flattenNetworksMessage);
			return true;
		}
		return false;
	}

	private void flush() {
		// System.out.println(this.toString());
		this.waiting = new HashSet<Integer>();
		this.got = new HashMap<HashSet<Integer>, Integer>();
	}

	@Override
	public String toString() {
		String result = "";
		result += "===============\n";
		result += "networks: " + this.networks.toString() + "\n";
		result += "flat: " + this.flattenNetworks.toString() + "\n";
		result += " + waiting: " + this.waiting.toString() + "\n";
		result += " + got: " + this.got.toString() + "\n";
		result += "===============\n";
		return result;
	}
}
