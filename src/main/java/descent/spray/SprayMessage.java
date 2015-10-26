package descent.spray;

import java.util.HashSet;
import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class SprayMessage implements IMessage {

	private List<Node> sample;
	private HashSet<Integer> networks;
	private Integer remember;

	public SprayMessage(List<Node> sample, HashSet<Integer> networks,
			Integer remember) {
		this.sample = sample;
		this.networks = (HashSet<Integer>) networks.clone();
		this.remember = new Integer(remember);
	}

	public Object getPayload() {
		return this.sample;
	}

	public HashSet<Integer> getNetworks() {
		return this.networks;
	}

	public Integer getRemember() {
		return remember;
	}
}
