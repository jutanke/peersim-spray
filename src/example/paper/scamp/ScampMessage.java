package example.paper.scamp;

import peersim.core.Node;

/**
 * Created by julian on 3/17/15.
 */
public final class ScampMessage {

    public enum Type {
        ForwardSubscription,
        Subscribe,
        Resubscribe,
        Accepted
    }

    // ==========================================
    // S T A T I C
    // ==========================================

    public static final ScampMessage subscribe(View.ViewEntry ve) {
        return new ScampMessage(ve.node, Type.Subscribe, ve, MAX_TTL);
    }

    public static final ScampMessage resubscribe(View.ViewEntry ve) {
        return new ScampMessage(ve.node, Type.Resubscribe, ve, MAX_TTL);
    }

    public static final ScampMessage forward(Node me, View.ViewEntry ve) {
        return new ScampMessage(me, Type.ForwardSubscription, ve, MAX_TTL);
    }

    public static final ScampMessage forward(Node me, ScampMessage m) {
        if (m.type != Type.ForwardSubscription && m.type != Type.Subscribe && m.type != Type.Resubscribe) {
            throw new RuntimeException("must be a forwarded subscription or a subscription, instead:" + m.type);
        }
        return new ScampMessage(me, Type.ForwardSubscription, m.subscriber, m.ttl - 1);
    }

    public static final ScampMessage accepted(Node me) {
        return new ScampMessage(me, Type.Accepted, null, MAX_TTL);
    }


    // ==========================================
    // O B J E C T
    // ==========================================

    private static final int MAX_TTL = 20;

    public final Node sender;
    public final Type type;
    public final View.ViewEntry subscriber;
    public final int ttl;

    private ScampMessage(Node sender, Type type, View.ViewEntry subscriber, int ttl) {
        this.type = type;
        this.sender = sender;
        this.subscriber = subscriber;
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "{type:" + this.type +
                ", sender:" + this.sender.getID() +
                ", subs:" + this.subscriber +
                ", ttl:" + this.ttl + "}";
    }

}
