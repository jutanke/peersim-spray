package example.scamp;

import peersim.core.Node;

/**
 * Created by julian on 29/01/15.
 */
public class ScampMessage {

    public enum Type {
        Subscribe,
        Unsubscribe,
        ForwardSubscription,
        AcceptedSubscription,
        WeightUpdate,
        RequestContact,
        GiveContact,
        HandleUnsubscribeIn,  // handles the nodes in-view
    }

    public final Type type;
    public final Node sender;
    public final Node subscriber;
    public int ttl;
    public final double weight;
    public final boolean updateInView;
    public Node contact;
    public int hop;
    public Node acceptor;
    public final Node replacer;

    protected ScampMessage(Node n, Type t, Node s) {
        this.ttl = 15;
        this.type = t;
        this.weight = -1.0;
        this.sender = n;
        this.subscriber = s;
        updateInView = false;
        this.replacer = null;
    }

    protected ScampMessage(Node newsender, ScampMessage m) {
        this.ttl = m.ttl;
        this.type = m.type;
        this.subscriber = m.subscriber;
        this.weight = m.weight;
        this.updateInView = m.updateInView;
        this.contact = m.contact;
        this.hop = m.hop;
        this.replacer = m.replacer;
        this.sender = newsender;
    }

    private ScampMessage(Node n, double weight, boolean updateInView) {
        this.weight = weight;
        this.sender = n;
        this.subscriber = null;
        this.type = Type.WeightUpdate;
        this.updateInView = updateInView;
        this.replacer = null;
    }

    private ScampMessage(Node sender, Node replacer, Type t) {
        this.replacer = replacer;
        this.sender = sender;
        this.type = t;
        this.weight = -1;
        this.updateInView = false;
        this.subscriber = null;
    }

    public static ScampMessage forward(Node sender, ScampMessage m) {
        ScampMessage message = new ScampMessage(sender, m);
        message.reduceTTL();
        return message;
    }

    public static ScampMessage acceptMessage(Node sender, Node subscriber, Node acceptor) {
        ScampMessage m = new ScampMessage(sender, Type.AcceptedSubscription, subscriber);
        m.acceptor = acceptor;
        return m;
    }

    public static ScampMessage updateInViewAfterUnsubscribe(Node sender, Node replacer) {
        return new ScampMessage(sender, replacer, Type.HandleUnsubscribeIn);
    }

    public static ScampMessage updateWeightMessageInView(Node sender, double weight) {
        return new ScampMessage(sender, weight, true);
    }

    public static ScampMessage updateWeightMessagePartialView(Node sender, double weight) {
        return new ScampMessage(sender, weight, false);
    }

    public static ScampMessage requestContact(Node sender, Node subscriber, int hop) {
        ScampMessage m = new ScampMessage(sender, Type.RequestContact, subscriber);
        m.hop = hop;
        return m;
    }

    public static ScampMessage giveContact(Node sender, Node subscriber, Node contact) {
        ScampMessage m = new ScampMessage(sender, Type.GiveContact, subscriber);
        m.contact = contact;
        return m;
    }


    public static ScampMessage createForwardSubscription(Node sender, Node subscriber) {
        return new ScampMessage(sender, Type.ForwardSubscription, subscriber);
    }

    public boolean isValid() {
        return this.ttl > 0;
    }

    public void reduceTTL() {
        this.ttl -= 1;
    }

    @Override
    public String toString() {
        return "Msg: {" +
                sender.getID() + "-> s:" +
                (subscriber == null ? "<null>" : subscriber.getID()) + " t:" +
                type + "}";
    }

}
