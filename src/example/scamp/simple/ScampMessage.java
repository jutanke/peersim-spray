package example.scamp.simple;

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
        WeightUpdate
    }

    public final Type type;
    public final Node sender;
    public final Node subscriber;
    public int ttl;
    public final double weight;

    public ScampMessage(Node n, Type t, Node s) {
        this.ttl = 250;
        this.type = t;
        this.weight = -1.0;
        this.sender = n;
        this.subscriber = s;
    }

    private ScampMessage(Node n, double weight) {
        this.weight = weight;
        this.sender = n;
        this.subscriber = null;
        this.type = Type.WeightUpdate;
    }

    public static ScampMessage updateWeightMessage(Node sender, double weight) {
        return new ScampMessage(sender, weight);
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
