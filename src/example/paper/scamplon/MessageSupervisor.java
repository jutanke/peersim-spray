package example.paper.scamplon;

import peersim.core.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Make sure that a message gets dropped when a child got removed and reinserted
 * as it is technically a different node now!
 * Created by julian on 3/30/15.
 */
public final class MessageSupervisor {
    private MessageSupervisor() {};

    private static final Map<String, Integer> lookup = new HashMap<String, Integer>();

    /**
     * always take the protocol direction
     *
     *    shuffle
     * a -----------> b
     *    shfflresp
     * a <----------- b
     *
     *  ==> "a_b"
     *
     * @param destination
     * @param m
     * @return
     */
    private static final String createKey(Node destination, ScamplonMessage m) {
        switch (m.type) {
            case Shuffle:
                return m.sender.getID() + "_" + destination.getID();
            case ShuffleResponse:
                return destination.getID() + "_" + m.sender.getID();
            default:
                throw new RuntimeException("ONLY SHUFFLES!");
        }
    }


    public static final void put(Node destination, ScamplonMessage m) {
        if (m.type != ScamplonMessage.Type.Shuffle) {
            throw new RuntimeException("MUST BE SHUFFLE");
        }
        String key = createKey(destination, m);
        final Scamplon sender = (Scamplon) m.sender.getProtocol(Scamplon.pid);
        lookup.put(key, sender.hash());
    }

    public static final boolean validate(Node destination, ScamplonMessage m) {
        if (m.type != ScamplonMessage.Type.ShuffleResponse) {
            throw new RuntimeException("MUST BE ShuffleResponse");
        }
        String key = createKey(destination, m);
        if (lookup.containsKey(key)) {
            final int hash = lookup.get(key);
            final Scamplon sender = (Scamplon) destination.getProtocol(Scamplon.pid);
            return hash == sender.hash();
        }
        return false;
    }

}
