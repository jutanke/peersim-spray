package descent.spray;

import java.util.HashSet;

public class MergingRegister {

	private HashSet<Integer> networkId;

	public MergingRegister() {
		this.networkId = new HashSet<Integer>();
	}

	public MergingRegister(HashSet<Integer> networkId) {
		this.networkId = new HashSet<Integer>(networkId);
	}

	public void initialize(Integer networkId) {
		this.networkId.add(networkId);
	}

	public HashSet<Integer> getNetworkId() {
		return networkId;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		MergingRegister registerClone = new MergingRegister(this.networkId);
		return registerClone;
	}
}
