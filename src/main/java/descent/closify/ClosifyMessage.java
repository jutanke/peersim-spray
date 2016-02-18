package descent.closify;

import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class ClosifyMessage implements IMessage {

	private List<Node> sample;

	public ClosifyMessage(List<Node> sample) {
		this.sample = sample;
	}

	public Object getPayload() {
		return this.sample;
	}

}
