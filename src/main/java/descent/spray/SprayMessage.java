package descent.spray;

import java.util.List;

import peersim.core.Node;
import descent.rps.IMessage;

public class SprayMessage implements IMessage {

	private List<Node> sample;
	private MergeRegister register;

	public SprayMessage(List<Node> sample, MergeRegister register) {
		this.sample = sample;
		this.register = new MergeRegister();
		this.register.networks = register.networks;
		this.register.size = new Integer(register.size);
	}

	public Object getPayload() {
		return this.sample;
	}

	public MergeRegister getMergeRegister() {
		return this.register;
	}
}
