package descent.spray;

import java.util.HashSet;

public class MergingRegister {

	private HashSet<Integer> networkId;

	private HashSet<MergingMessage> pending;

	public MergingRegister() {
		this.networkId = new HashSet<Integer>();
		this.pending = new HashSet<MergingMessage>();
	}

	public MergingRegister(HashSet<Integer> networkId) {
		this.networkId = new HashSet<Integer>(networkId);
		this.pending = new HashSet<MergingMessage>();
	}

	public void initialize(Integer networkId) {
		this.networkId.add(networkId);
	}

	public HashSet<Integer> getNetworkId() {
		return networkId;
	}

	public boolean isToMerge(MergingMessage m) {
		return !this.networkId.containsAll(m.getNetworkId()) && (!this.pending.contains(m));
	}

	public void add(MergingMessage m) {
		this.pending.add(m);
	}

	public boolean shouldMerge() {
		return !this.pending.isEmpty();
	}

	public double getArcNumber(MergingMessage m) {
		// (TODO) get the arc number
		// (TODO) add all ids to current network

		this.pending.clear();
		return 0.;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		MergingRegister registerClone = new MergingRegister(this.networkId);
		return registerClone;
	}
}
