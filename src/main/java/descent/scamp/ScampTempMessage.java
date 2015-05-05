package descent.scamp;

import descent.rps.IMessage;

public class ScampTempMessage implements IMessage {

	private int occ; // number of identities to spread in the network

	public ScampTempMessage(int occ) {
		this.occ = occ;
	}

	public Object getPayload() {
		return this.occ;
	}

}
