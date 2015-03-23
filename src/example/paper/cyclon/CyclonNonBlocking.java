package example.paper.cyclon;

import peersim.core.Fallible;
import peersim.core.Node;

/**
 * Created by julian on 3/23/15.
 */
public class CyclonNonBlocking extends CyclonProtocol {

    // ===========================================
    // C T O R
    // ===========================================

    public CyclonNonBlocking(String prefix) {
        super(prefix);
    }

    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void processMessage(Node me, CyclonMessage message) {
        if (me.isUp()) {



        }
    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        if (node.isUp()) {

            this.increaseAge();


        }
    }

    // ===========================================
    // P R I V A T E
    // ===========================================
}
