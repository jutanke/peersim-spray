package descent.spray;

import java.util.HashSet;
import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class SprayMessage implements IMessage {

	private List<Node> sample;
	private HashSet<Integer> networks;

	public SprayMessage(List<Node> sample, HashSet<Integer> networks) {
		this.sample = sample;
		this.networks = (HashSet<Integer>) networks.clone();
	}

	public Object getPayload() {
		return this.sample;
	}

	public HashSet<Integer> getNetworks() {
		return this.networks;
	}
}
