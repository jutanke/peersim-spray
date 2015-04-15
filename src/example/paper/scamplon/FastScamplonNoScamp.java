package example.paper.scamplon;

import peersim.core.CommonState;
import peersim.core.Node;

/**
 * Created by julian on 4/15/15.
 */
public class FastScamplonNoScamp extends FastScamplon {

    public FastScamplonNoScamp(String prefix) {
        super(prefix);
    }

    public static void subscribe(final Node s, final Node c) {
        final FastScamplonNoScamp subscriber = (FastScamplonNoScamp) s.getProtocol(pid);
        subscriber.inView.clear();
        subscriber.partialView.clear();

        final FastScamplonNoScamp contact = (FastScamplonNoScamp) c.getProtocol(pid);

        if (subscriber.isUp() && contact.isUp()) {

            subscriber.addNeighbor(c);
            for (Node n : contact.getPeers()) {
                insert(s, n);
            }

            for (int i = 0; i < FastScamplon.c && contact.degree() > 0; i++) {
                final Node n = contact.getNeighbor(CommonState.r.nextInt(contact.degree()));
                insert(s, n);
            }

        } else {
            throw new RuntimeException("qq");
        }

    }

    private static boolean insert(Node s, Node n) {
        final FastScamplonNoScamp subscriber = (FastScamplonNoScamp) s.getProtocol(pid);
        if (n.getID() != s.getID()) {
            final FastScamplonNoScamp current = (FastScamplonNoScamp) n.getProtocol(pid);
            if (current.isUp()) {
                current.addNeighbor(s);
                subscriber.addToInview(s, n);
                return true;
            }
        }
        return false;
    }
}
