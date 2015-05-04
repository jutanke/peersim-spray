package descent.cyclon;

import peersim.core.Node;
import descent.controllers.ChurnProtocol;

/**
 * Created by julian on 4/1/15.
 */
public class Churn extends ChurnProtocol {

	public Churn(String prefix) {
		super(prefix, CyclonTemp.PAR_PROT);
		ChurnProtocol.current = this;
	}

	@Override
	public void removeNode(Node leaver) {
		CyclonTemp leaverCyclon = (CyclonTemp) leaver
				.getProtocol(CyclonTemp.pid);
		leaverCyclon.leave();
	}

	@Override
	public void addNode(Node joiner, Node contact) {
		CyclonTemp joinerCyclon = (CyclonTemp) joiner
				.getProtocol(CyclonTemp.pid);
		joinerCyclon.join(joiner, contact);
	}
}
