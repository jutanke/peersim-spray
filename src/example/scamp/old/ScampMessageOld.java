package example.scamp.old;

import peersim.core.Node;

/**
 * Created by julian on 29/01/15.
 */
public class ScampMessageOld {

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

    protected ScampMessageOld(Node n, Type t, Node s) {
        this.ttl = 100;
        this.type = t;
        this.weight = -1.0;
        this.sender = n;
        this.subscriber = s;
        updateInView = false;
        this.replacer = null;
    }

    protected ScampMessageOld(Node newsender, ScampMessageOld m) {
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

    private ScampMessageOld(Node n, double weight, boolean updateInView) {
        this.weight = weight;
        this.sender = n;
        this.subscriber = null;
        this.type = Type.WeightUpdate;
        this.updateInView = updateInView;
        this.replacer = null;
    }

    private ScampMessageOld(Node sender, Node replacer, Type t) {
        this.replacer = replacer;
        this.sender = sender;
        this.type = t;
        this.weight = -1;
        this.updateInView = false;
        this.subscriber = null;
    }

    public static ScampMessageOld forward(Node sender, ScampMessageOld m) {
        ScampMessageOld message = new ScampMessageOld(sender, m);
        message.reduceTTL();
        return message;
    }

    public static ScampMessageOld acceptMessage(Node sender, Node subscriber, Node acceptor) {
        ScampMessageOld m = new ScampMessageOld(sender, Type.AcceptedSubscription, subscriber);
        m.acceptor = acceptor;
        return m;
    }

    public static ScampMessageOld updateInViewAfterUnsubscribe(Node sender, Node replacer) {
        return new ScampMessageOld(sender, replacer, Type.HandleUnsubscribeIn);
    }

    public static ScampMessageOld updateWeightMessageInView(Node sender, double weight) {
        return new ScampMessageOld(sender, weight, true);
    }

    public static ScampMessageOld updateWeightMessagePartialView(Node sender, double weight) {
        return new ScampMessageOld(sender, weight, false);
    }

    public static ScampMessageOld requestContact(Node sender, Node subscriber, int hop) {
        ScampMessageOld m = new ScampMessageOld(sender, Type.RequestContact, subscriber);
        m.hop = hop;
        return m;
    }

    public static ScampMessageOld giveContact(Node sender, Node subscriber, Node contact) {
        ScampMessageOld m = new ScampMessageOld(sender, Type.GiveContact, subscriber);
        m.contact = contact;
        return m;
    }


    public static ScampMessageOld createForwardSubscription(Node sender, Node subscriber) {
        return new ScampMessageOld(sender, Type.ForwardSubscription, subscriber);
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
