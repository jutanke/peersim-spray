package example.paper.scamplon;

import peersim.core.Node;

/**
 * Created by julian on 4/15/15.
 */
public class FastChurnNoScamp extends FastChurn {

    public FastChurnNoScamp(String n) {
        super(n);
    }

    @Override
    public void addNode(Node s, Node c) {
        FastScamplonNoScamp.subscribe(s, c);
    }
}
