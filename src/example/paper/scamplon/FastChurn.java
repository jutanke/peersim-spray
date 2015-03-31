package example.paper.scamplon;

import example.Scamplon.*;
import example.Scamplon.ScamplonProtocol;
import example.paper.ChurnProtocol;
import peersim.core.Node;

/**
 * Created by julian on 3/31/15.
 */
public class FastChurn extends ChurnProtocol {

    public FastChurn(String n) {
        super(n, ScamplonProtocol.SCAMPLON_PROT);
        ChurnProtocol.current = this;
    }

    @Override
    public void removeNode(Node node) {
        FastScamplon.unsubscribe(node);
    }

    @Override
    public void addNode(Node subscriber, Node contact) {
        FastScamplon.subscribe(subscriber, contact);
    }
}
