package descent.scamp;

import peersim.core.Node;
import descent.ChurnProtocol;

/**
 * Created by julian on 4/15/15.
 */
public class Churn extends ChurnProtocol{

    public Churn(String n) {
        super(n, Scamp.SCAMP_PROT);
        ChurnProtocol.current = this;
    }

    @Override
    public void removeNode(Node node) {
        Scamp.unsubscribe(node);
    }

    @Override
    public void addNode(Node subscriber, Node contact) {
        Scamp.subscribe(subscriber, contact);
    }
}
