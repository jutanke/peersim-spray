package descent.spray;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class SprayMessage implements IMessage {

	private List<Node> sample;

	public LinkedList<HashSet<Integer>> networks;
	public Integer size;

	public SprayMessage(List<Node> sample,
			LinkedList<HashSet<Integer>> networks, Integer size) {
		this.sample = sample;
		this.networks = (LinkedList<HashSet<Integer>>) networks.clone();
		this.size = new Integer(size);
	}

	public Object getPayload() {
		return this.sample;
	}

}
