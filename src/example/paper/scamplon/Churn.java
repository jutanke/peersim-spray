package example.paper.scamplon;

import example.Scamplon.ScamplonProtocol;
import example.paper.ChurnProtocol;
import peersim.core.Node;

/**
 * Created by julian on 3/27/15.
 */
public class Churn extends ChurnProtocol {

    public Churn(String n) {
        super(n, ScamplonProtocol.SCAMPLON_PROT);
    }

    @Override
    public void addNode(Node subscriber, Node contact) {
        Scamplon.subscribe(subscriber, contact);
    }
}
