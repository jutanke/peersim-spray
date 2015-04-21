package descent.cyclon;

import peersim.core.Node;
import descent.ChurnProtocol;

/**
 * Created by julian on 4/1/15.
 */
public class Churn extends ChurnProtocol {

	public Churn(String n) {
		super(n, CyclonProtocol.PAR_PROT);
		ChurnProtocol.current = this;
	}

	@Override
	public void removeNode(Node node) {
		// if (CommonState.getTime() == 2210) {
		// System.err.println("remove @" + node.getID());
		// }
		Cyclon.removeFromNetwork(node);
	}

	@Override
	public void addNode(Node subscriber, Node contact) {
		Cyclon.add(subscriber, contact);
	}
}
