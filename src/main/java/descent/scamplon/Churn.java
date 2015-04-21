package descent.scamplon;

import peersim.core.Node;
import descent.ChurnProtocol;

/**
 * Created by julian on 3/31/15.
 */
public class Churn extends ChurnProtocol {

	public Churn(String n) {
		super(n, ScamplonProtocol.SCAMPLON_PROT);
		ChurnProtocol.current = this;
	}

	@Override
	public void removeNode(Node node) {
		Scamplon.unsubscribe(node);
	}

	@Override
	public void addNode(Node subscriber, Node contact) {
		Scamplon.subscribe(subscriber, contact);
	}
}
