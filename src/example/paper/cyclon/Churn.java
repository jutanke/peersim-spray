package example.paper.cyclon;

import example.paper.ChurnProtocol;
import peersim.core.*;

import java.util.*;

/**
 * Created by julian on 3/27/15.
 */
public class Churn extends ChurnProtocol {

    public Churn(String n) {
        super(n, CyclonProtocol.PAR_PROT);
        ChurnProtocol.current = this;
    }

    @Override
    public void removeNode(Node node) {
    }

    @Override
    public void addNode(Node subscriber, Node contact) {
        CyclonProtocol pp = (CyclonProtocol) subscriber.getProtocol(this.pid);
        pp.addNeighbor(contact);
    }
}
