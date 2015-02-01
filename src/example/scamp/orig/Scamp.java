package example.scamp.orig;

import example.scamp.ScampProtocol;
import example.scamp.ScampWithView;
import example.scamp.View;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 01/02/15.
 */
public class Scamp extends ScampWithView {

    // ===================================================
    // E N T I T Y
    // ===================================================

    public Scamp(String s) {
        super(s);
    }

    @Override
    public Object clone() {
        Scamp s = (Scamp) super.clone();
        return s;
    }

    // ===================================================
    // P U B L I C  I N T E R F A C E
    // ===================================================

    @Override
    public void join(Node me, Node subscriber) {

    }

    @Override
    public void rejoin(Node me) {

    }

    @Override
    public void unsubscribe(Node me) {

    }

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================
}
