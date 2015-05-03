package descent.cyclon;

import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

/**
 * Message containing the sample to exchange in Cyclon
 */
public class CyclonTempMessage implements IMessage {

	private List<Node> sample;

	public CyclonTempMessage(List<Node> sample) {
		this.sample = sample;
	}

	public Object getPayload() {
		return this.sample;
	}
}
