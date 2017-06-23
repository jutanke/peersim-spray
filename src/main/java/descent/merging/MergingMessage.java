package descent.merging;

import java.util.HashSet;

/**
 * Message broadcast by the joiner during a merging of two networks
 *
 */
public class MergingMessage {

	private final HashSet<Integer> networkId;

	private final Double a;
	private final Double b;

	private final Double value;

	/**
	 * 
	 * @param networkId
	 *            The identifier of the network of the joiner
	 * @param value
	 *            partial view size or an aggregation of it
	 * @param a
	 *            the multiplicative factor of the joining network a*ln(N)+b
	 * @param b
	 *            see the above formula
	 */
	public MergingMessage(HashSet<Integer> networkId, Double value, Double a, Double b) {
		this.networkId = new HashSet<Integer>(networkId);
		this.a = a;
		this.b = b;
		this.value = value;
	}

	public Double getA() {
		return a;
	}

	public Double getB() {
		return b;
	}

	public Double getValue() {
		return value;
	}

	public HashSet<Integer> getNetworkId() {
		return networkId;
	}
}
