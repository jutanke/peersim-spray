package example.paper.scamp;

import example.paper.ChurnProtocol;
import peersim.core.Node;

/**
 * Created by julian on 4/15/15.
 */
public class FastChurn extends ChurnProtocol{

    public FastChurn(String n) {
        super(n, FastScamp.SCAMP_PROT);
        ChurnProtocol.current = this;
    }

    @Override
    public void removeNode(Node node) {
        FastScamp.unsubscribe(node);
    }

    @Override
    public void addNode(Node subscriber, Node contact) {
        FastScamp.subscribe(subscriber, contact);
    }
}
