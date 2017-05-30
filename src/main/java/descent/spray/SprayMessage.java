package descent.spray;

import java.util.List;

import descent.rps.IMessage;
import peersim.core.Node;

public class SprayMessage implements IMessage {

	private List<Node> sample;

	public SprayMessage(List<Node> sample) {
		this.sample = sample;
	}

	public Object getPayload() {
		return this.sample;
	}

}
