package descent.spray;

import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class SprayTempMessage implements IMessage {

	private List<Node> sample;

	public SprayTempMessage(List<Node> sample) {
		this.sample = sample;
	}

	public Object getPayload() {
		return this.sample;
	}
}
