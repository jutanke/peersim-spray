package descent.spray;

import peersim.core.Node;
import descent.controllers.ChurnProtocol;
import descent.rps.ARandomPeerSamplingProtocol;

/**
 * Created by julian on 3/31/15.
 */
public class Churn extends ChurnProtocol {

	public Churn(String n) {
		super(n, ARandomPeerSamplingProtocol.PAR_PROT);
		ChurnProtocol.current = this;
	}

	@Override
	public void removeNode(Node leaver) {
		SprayTemp leaverSpray = (SprayTemp) leaver.getProtocol(SprayTemp.pid);
		leaverSpray.leave();
	}

	@Override
	public void addNode(Node joiner, Node contact) {
		SprayTemp joinerSpray = (SprayTemp) joiner.getProtocol(SprayTemp.pid);
		joinerSpray.join(joiner, contact);
	}
}
