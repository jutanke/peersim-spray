package descent.tman;

import java.util.List;

import descent.rps.IMessage;
import peersim.core.Node;

public class TManMessage implements IMessage {

	private final List<Node> sample;

	public TManMessage(List<Node> sample) {
		this.sample = sample;
	}

	public Object getPayload() {
		return this.sample;
	}

}
