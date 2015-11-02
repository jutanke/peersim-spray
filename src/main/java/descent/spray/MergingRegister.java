package descent.spray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MergingRegister {

	public LinkedList<HashSet<Integer>> networks;
	public HashSet<Integer> flattenNetworks;
	public Integer size;

	public HashSet<Integer> waiting;
	public HashMap<HashSet<Integer>, Integer> got; // NetworkId -> size

	public MergingRegister() {
		this.networks = new LinkedList<HashSet<Integer>>();
		this.size = -1;

		this.waiting = new HashSet<Integer>();
		this.got = new HashMap<HashSet<Integer>, Integer>();
	}

	public boolean isMerge(SprayMessage m, Integer currentSize) {
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
		this.waiting.addAll(flattenNetworksMessage);
		
		// #2 process the size given by the message
		// #A (TODO)
		return false;
	}

	private void flush() {
		this.waiting = new HashSet<Integer>();
		this.got = new HashMap<HashSet<Integer>, Integer>();
	}
}
