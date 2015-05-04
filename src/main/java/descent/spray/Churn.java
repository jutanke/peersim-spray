package descent.spray;

import peersim.core.Node;
import descent.controllers.ChurnProtocol;

/**
 * Created by julian on 3/31/15.
 */
public class Churn extends ChurnProtocol {

	public Churn(String n) {
		super(n, SprayProtocol.SCAMPLON_PROT);
		ChurnProtocol.current = this;
	}

	@Override
	public void removeNode(Node node) {
		Spray.unsubscribe(node);
	}

	@Override
	public void addNode(Node subscriber, Node contact) {
		Spray.subscribe(subscriber, contact);
	}
}
