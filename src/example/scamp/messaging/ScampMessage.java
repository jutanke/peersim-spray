package example.scamp.messaging;

import example.cyclon.PeerSamplingService;
import peersim.core.Node;

import java.util.LinkedList;
import java.util.Stack;

/**
 * Created by julian on 01/02/15.
 */
public class ScampMessage {

    public enum Type {
        ForwardSubscriptionHandshake,
        ForwardSubscription,
        AcceptSubscription,
        KeepAlive,
        IndirectionHandshake,
        Answer,
        Handshake
    }

    // ==================================================
    // E X T E R N A L  I N T E R F A C E
    // ==================================================

    public static ScampMessage createIndirectionAnswer(Node sender, Node subscriber, Node contact) {
        ScampMessage message = new ScampMessage(sender, Type.IndirectionHandshake);
        message.payload = subscriber;
        message.payload2 = contact;
        return message;
    }

    /**
     * Tell everybody the new age!
     * @param sender
     * @return
     */
    public static ScampMessage createKeepAlive(Node sender, long newBirthDate) {
        ScampMessage message = new ScampMessage(sender, newBirthDate);
        return message;
    }

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

    /**
     *
     * @param sender
     * @param subscriber
     * @return
     */
    public static ScampMessage createForwardSubscriptionHandshake(Node sender, Node subscriber) {
        ScampMessage message = new ScampMessage(sender, Type.ForwardSubscriptionHandshake);
        message.payload = subscriber;
        message.route = new Stack<Node>();
        message.route.push(sender);
        return message;
    }


    public static ScampMessage updateForwardSubscriptionHandshake(Node sender, ScampMessage message) {
        if (message.type != Type.ForwardSubscriptionHandshake) {
            throw new RuntimeException("Wrong type: " + message.type);
        }
        ScampMessage result = new ScampMessage(sender, (message.ttl - 1), message.type);
        result.payload = message.payload;
        result.route = message.route;
        result.route.push(sender);
        return message;
    }


    public static ScampMessage createAnswer(Node sender, ScampMessage offer) {
        if (offer.type != Type.ForwardSubscriptionHandshake) {
            throw new RuntimeException("wrong message type:" + offer.type);
        }
        ScampMessage message = new ScampMessage(sender, Type.Answer);

        message.route = offer.route;        // the route back to the offerer!
        message.route2 = new Stack<Node>(); // the route back to me!
        message.route2.push(sender);

        return message;
    }

    // ==================================================
    // I N T E R N A L  I N T E R F A C E
    // ==================================================

    public final Type type;
    public final long newBirthDate;
    public final Node sender;
    public Node payload, payload2;
    private final int ttl;
    public Stack<Node> route;
    public Stack<Node> route2;


    public static final int START_TTL = 150;

    private ScampMessage(Node sender, int ttl, Type type) {
        this.sender = sender;
        this.ttl = ttl;
        this.type = type;
        this.newBirthDate = -1;
    }

    private ScampMessage(Node sender, Type type) {
        this.sender = sender;
        this.ttl = START_TTL;
        this.type = type;
        this.newBirthDate = -1;
    }

    private ScampMessage(Node sender, long birthDate) {
        this.newBirthDate = birthDate;
        this.sender = sender;
        this.ttl = START_TTL;
        this.type = Type.KeepAlive;
    }


    @Override
    public String toString(){
        return "msg: " + type + " sender:" + this.sender.getID() + " payload1:" +
                ((payload == null) ? "<null>" : payload.getID()) + " payload2" +
                ((payload2 == null) ? "<null>" : payload2.getID()) + " ttl:" + ttl;
    }

    public boolean isExpired() {
        return this.ttl <= 0;
    }
}
