package example.scamp.messaging;

import example.cyclon.PeerSamplingService;
import example.scamp.ScampProtocol;
import peersim.core.Network;
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
        Handshake,
        Connect,
        Loop
    }

    public enum LoopTopic {
        Nothing,
        Indirection,
        BacktrackConnection,
        forwardSubscription,
        AcceptSubscription
    }

    // ==================================================
    // E X T E R N A L  I N T E R F A C E
    // ==================================================

    public static ScampMessage smallLoop(Node sender, LoopTopic t, Node destination) {
        ScampMessage message = new ScampMessage(sender, Type.Loop);
        message.loopCounter = (int)Math.max(Math.log(Network.size()), 1);
        //message.loopCounter = 0;
        message.payload = destination;
        message.topic = t;
        return message;
    }

    @Deprecated
    public static ScampMessage bigLoop(Node sender) {
        ScampMessage message = new ScampMessage(sender, Type.Loop);
        message.loopCounter = (int)Math.max(Math.log(Network.size()), 1) + ScampProtocol.c;
        return message;
    }

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
        message.route.push(subscriber);
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

    /**
     *
     * @param sender
     * @param offer
     * @return
     */
    public static ScampMessage createAnswer(Node sender, ScampMessage offer) {
        if (offer.type != Type.ForwardSubscriptionHandshake) {
            throw new RuntimeException("wrong message type:" + offer.type);
        }
        ScampMessage message = new ScampMessage(sender, Type.Answer);
        message.route = offer.route;        // the route back to the offerer!
        message.route2 = new Stack<Node>(); // the route back to me!
        message.route2.push(sender);
        message.payload = offer.payload;    // the subscriber
        message.payload2 = sender;           // define "me"

        return message;
    }

    public static ScampMessage updateAnswer(Node sender, ScampMessage answer) {
        if (answer.type != Type.Answer) {
            throw new RuntimeException("wrong message type2:" + answer.type);
        }
        ScampMessage message = new ScampMessage(sender,(answer.ttl - 1), Type.Answer);
        message.route = answer.route;
        message.route2 = answer.route2;
        message.route2.push(sender);
        message.payload2 = answer.payload2;
        message.payload = answer.payload;
        return message;
    }

    /**
     *
     * @param sender
     * @param answer
     * @return
     */
    public static ScampMessage createHandshake(Node sender, ScampMessage answer) {
        if (answer.type != Type.Answer) {
            throw new RuntimeException("wrong message type:" + answer.type);
        }
        ScampMessage message = new ScampMessage(sender, Type.Handshake);
        message.route = answer.route2;      // the route back
        message.payload = answer.payload2;  // the target
        message.payload2 = answer.payload;  // the subscriber
        return message;
    }

    public static ScampMessage updateHandshake(Node sender, ScampMessage handshake) {
        if (handshake.type != Type.Answer) {
            throw new RuntimeException("wrong message type3:" + handshake.type);
        }
        ScampMessage message = new ScampMessage(sender,(handshake.ttl - 1), handshake.type);
        message.route = handshake.route;
        message.payload = handshake.payload;
        message.payload2 = handshake.payload2;
        return message;
    }

    /**
     *
     * @param sender
     * @param handshake
     * @return
     */
    public static ScampMessage createConnect(Node sender, ScampMessage handshake) {
        if (handshake.type != Type.Handshake) {
            throw new RuntimeException("wrong message type:" + handshake.type);
        }
        return new ScampMessage(sender, Type.Connect);
    }

    public static ScampMessage copy(ScampMessage m) {
        ScampMessage result = new ScampMessage(m.sender, m.ttl, m.type);
        result.payload = m.payload;
        result.payload2 = m.payload2;
        return result;
    }

    // ==================================================
    // I N T E R N A L  I N T E R F A C E
    // ==================================================

    public LoopTopic topic = LoopTopic.Nothing;
    public final Type type;
    public final long newBirthDate;
    public final Node sender;
    public Node payload, payload2;
    public final int ttl;
    public Stack<Node> route;
    public Stack<Node> route2;
    public int loopCounter;

    /**
     * @return True-> exit, otherwise false
     */
    public boolean refreshLoopCounter() {
        this.loopCounter -= 1;
        return this.loopCounter == 0;
    }

    public boolean keepLooping() {
        return this.loopCounter >= 0;
    }

    public static final int START_TTL = 20;

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
                ((payload2 == null) ? "<null>" : payload2.getID()) + " ttl:" + ttl +
                ((route == null) ? "" : " route:" + routeToString(route));
    }

    public String debugLoop() {
        return "counter:" + this.loopCounter + " for " + this.topic + " from " + this.sender.getID();
    }

    public boolean isExpired() {
        return this.ttl <= 0;
    }

    private String routeToString (Stack<Node> stack) {
        Stack<Node> copy = (Stack)stack.clone();
        StringBuilder sb = new StringBuilder();
        sb.append("--> [");
        while (!copy.empty()) {
            sb.append(" ");
            sb.append(copy.pop().getID());
        }
        sb.append("]");
        return sb.toString();
    }
}
