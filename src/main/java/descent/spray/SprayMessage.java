package descent.spray;

import java.util.HashSet;
import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class SprayMessage implements IMessage {

	private List<Node> sample;
	private HashSet<Integer> from;
	private Integer remember;
	private HashSet<Integer> to;

	public SprayMessage(List<Node> sample, HashSet<Integer> from,
			Integer remember, HashSet<Integer> to) {
		this.sample = sample;
		this.from = (HashSet<Integer>) from.clone();
		this.remember = new Integer(remember);
		this.to = (HashSet<Integer>) to.clone();
	}

	public Object getPayload() {
		return this.sample;
	}

	public HashSet<Integer> getFrom() {
		return this.from;
	}

	public Integer getRemember() {
		return this.remember;
	}

	public HashSet<Integer> getTo() {
		return this.to;
	}
}
