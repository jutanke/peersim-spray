package example.paper.scamp;

import peersim.core.Node;

/**
 * Created by julian on 3/17/15.
 */
public final class ScampMessage {

    public enum Type {
        ForwardSubscription,
        Subscribe,
        Accepted
    }

    // ==========================================
    // S T A T I C
    // ==========================================

    public static final ScampMessage subscribe(View.ViewEntry ve) {
        return new ScampMessage(ve.node, Type.Subscribe, ve);
    }

    public static final ScampMessage forward(Node me, View.ViewEntry ve) {
        return new ScampMessage(me, Type.ForwardSubscription, ve);
    }

    public static final ScampMessage forward(Node me, ScampMessage m) {
        if (m.type != Type.ForwardSubscription && m.type != Type.Subscribe) {
            throw new RuntimeException("must be a forwarded subscription or a subscription, instead:" + m.type);
        }
        return new ScampMessage(me, Type.ForwardSubscription, m.subscriber);
    }

    public static final ScampMessage accepted(Node me) {
        return new ScampMessage(me, Type.Accepted, null);
    }


    // ==========================================
    // O B J E C T
    // ==========================================

    public final Node sender;
    public final Type type;
    public final View.ViewEntry subscriber;
    public int ttl = 20;

    private ScampMessage(Node sender, Type type, View.ViewEntry subscriber) {
        this.type = type;
        this.sender = sender;
        this.subscriber = subscriber;
    }

}
