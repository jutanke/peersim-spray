package example.scamp.simple;

import example.scamp.ScampWithView;
import example.scamp.messaging.*;
import example.scamp.messaging.ScampMessage;
import peersim.core.Node;

/**
 * Created by julian on 2/2/15.
 */
public class ScampHandshake extends ScampWithView {

    // ===================================================
    // E N T I T Y
    // ===================================================

    public ScampHandshake(String s) {
        super(s);
    }

    // ===================================================
    // P U B L I C  I N T E R F A C E
    // ===================================================

    @Override
    public void handleSubscription(Node n, example.scamp.ScampMessage m) {

    }

    @Override
    public void subRejoin(Node me, long newBirthDate) {

    }

    @Override
    public void subNextCycle(Node node) {

    }

    @Override
    public void subDoSubscribe(Node acceptor, Node subscriber) {

    }

    @Override
    public void join(Node me, Node subscriber) {

    }

    @Override
    public void unsubscribe(Node me) {

    }

    @Override
    public void subProcessEvent(Node node, ScampMessage message) {

    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================
}
