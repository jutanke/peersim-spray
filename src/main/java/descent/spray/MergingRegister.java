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
		this.newNetworkDetected(m);
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

		if (permutation == null) {
			return 0.0; // (TODO) un-uglify this kind of code
		}

		Double networkSize = Math.exp(currentSize);
		for (HashSet<Integer> network : permutation) {
			networkSize += Math.exp(this.got.get(network));
		}

		Double result = this.oneParameterFormula(Math.exp(currentSize)
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
		Iterator<HashSet<Integer>> iNetworks = this.got.keySet().iterator();
		while (iNetworks.hasNext()) {
			HashSet<Integer> networkExamined = iNetworks.next();
			if (objective.containsAll(networkExamined)) {
				HashSet<Integer> cloneObjective = (HashSet<Integer>) objective
						.clone();
				cloneObjective.removeAll(networkExamined);
				HashSet<HashSet<Integer>> cloneCurrent = (HashSet<HashSet<Integer>>) current
						.clone();
				cloneCurrent.add(networkExamined);
				HashSet<HashSet<Integer>> result = this.permutations(
						cloneObjective, cloneCurrent);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public boolean keepSize(SprayMessage m) {
		HashSet<Integer> idNetworkSize = m.networks.getFirst();
		if (m.networks.size() > 1) {
			idNetworkSize = m.networks.get(1);
		}
		if (this.waiting.containsAll(idNetworkSize)
				&& !this.got.containsKey(idNetworkSize)) {
			this.got.put((HashSet<Integer>) idNetworkSize.clone(), m.size);
			return true;
		}
		return false;
	}

	public boolean newNetworkDetected(SprayMessage m) {
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
		this.waiting = new HashSet<Integer>();
		this.got = new HashMap<HashSet<Integer>, Integer>();
	}
}
