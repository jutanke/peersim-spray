package example.scamp;

import peersim.core.Control;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

/**
 * CODE TAKEN FROM: https://github.com/csko/Peersim/tree/master/scamp
 *
 *
 * Created by julian on 28/01/15.
 */
public class Subscribe implements Control, NodeInitializer {

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public void initialize(Node n) {

    }
}
