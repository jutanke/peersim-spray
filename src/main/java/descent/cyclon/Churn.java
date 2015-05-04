package descent.cyclon;

import peersim.core.Node;
import descent.controllers.ChurnProtocol;

/**
 * Created by julian on 4/1/15.
 */
public class Churn extends ChurnProtocol {

	public static Integer call = 0;

	public Churn(String prefix) {
		super(prefix, Cyclon.PAR_PROT);
		ChurnProtocol.current = this;
	}

	@Override
	public void removeNode(Node leaver) {
		Cyclon leaverCyclon = (Cyclon) leaver
				.getProtocol(Cyclon.pid);
		leaverCyclon.leave();
	}

	@Override
	public void addNode(Node joiner, Node contact) {
		Cyclon joinerCyclon = (Cyclon) joiner
				.getProtocol(Cyclon.pid);
		joinerCyclon.join(joiner, contact);
	}
}
