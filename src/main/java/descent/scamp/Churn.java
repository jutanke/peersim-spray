package descent.scamp;

import peersim.core.Node;
import descent.controllers.ChurnProtocol;

/**
 * Created by julian on 4/15/15.
 */
public class Churn extends ChurnProtocol {

	public Churn(String n) {
		super(n, ScampTemp.PAR_PROT);
		ChurnProtocol.current = this;
	}

	@Override
	public void removeNode(Node leaver) {
		ScampTemp leaverScamp = (ScampTemp) leaver.getProtocol(ScampTemp.pid);
		leaverScamp.leave();
	}

	@Override
	public void addNode(Node joiner, Node contact) {
		ScampTemp joinerScamp = (ScampTemp) joiner.getProtocol(ScampTemp.pid);
		joinerScamp.join(joiner, contact);
	}
}
