package descent.spray;

import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class SprayMessage implements IMessage {

	private List<Node> sample;

	public SprayMessage(List<Node> sample) {
		this.sample = sample;
	}

	public Object getPayload() {
		return this.sample;
	}
}
