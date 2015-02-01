package example.scamp.orig.messaging;

import peersim.core.Node;

/**
 * Created by julian on 01/02/15.
 */
public class ScampMessage {

    public enum Type {
        ForwardSubscription,
        AcceptSubscription
    }

    // ==================================================
    // E X T E R N A L  I N T E R F A C E
    // ==================================================

    public static ScampMessage createAccept(Node sender, Node subscriber, Node acceptor) {
        ScampMessage message = new ScampMessage(sender, Type.AcceptSubscription);
        message.payload = acceptor;
        message.payload2 = subscriber;
        return message;
    }

    /**
     * Create new message with max ttl
     * @param sender
     * @param subscriber
     * @return
     */
    public static ScampMessage createForwardSubscription(Node sender, Node subscriber) {
        ScampMessage message = new ScampMessage(sender, Type.ForwardSubscription);
        message.payload = subscriber;
        return message;
    }

    /**
     * Updates, reduces ttl by one
     * @param sender
     * @param message
     * @return
     */
    public static ScampMessage updateForwardSubscription(Node sender, ScampMessage message) {
        if (message.type != Type.ForwardSubscription) {
            throw new RuntimeException("Wrong type: " + message.type);
        }
        ScampMessage result = new ScampMessage(sender, (message.ttl - 1), message.type);
        result.payload = message.payload;
        return result;
    }


    // ==================================================
    // I N T E R N A L  I N T E R F A C E
    // ==================================================

    public final Type type;
    public final Node sender;
    public Node payload, payload2;
    private final int ttl;


    public static final int START_TTL = 100;

    private ScampMessage(Node sender, int ttl, Type type) {
        this.sender = sender;
        this.ttl = ttl;
        this.type = type;
    }

    private ScampMessage(Node sender, Type type) {
        this.sender = sender;
        this.ttl = START_TTL;
        this.type = type;
    }



    public boolean isExpired() {
        return this.ttl <= 0;
    }
}
