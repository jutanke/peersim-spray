package example.paper.scamplon;

import example.Scamplon.PartialView;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 3/25/15.
 */
public class ScamplonMessage {

    public enum Type {
        Shuffle,
        ShuffleResponse,
        Accept,
        Subscribe,
        Forward
    }

    public static final int INIT_TTL = 25;

    public final Type type;
    public final Node sender;
    public final int ttl;
    public final int secret;
    public List<PartialView.Entry> a;
    public List<PartialView.Entry> b;
    public Node subscriber;

    private ScamplonMessage(Type t, Node s, int ttl, int secret) {
        this.type = t;
        this.secret = secret;
        this.ttl = ttl;
        this.sender = s;
    }

    public boolean isDead() {
        return this.ttl < 0;
    }

    @Override
    public String toString() {
        return "type:" + this.type + ", sender:" + this.sender.getID();
    }

    // =======================================================
    // F A C T O R Y
    // =======================================================

    public static ScamplonMessage accept(Node sender) {
        return new ScamplonMessage(Type.Accept, sender, INIT_TTL, -1);
    }

    public static ScamplonMessage subscribe(Node sender) {
        return new ScamplonMessage(Type.Subscribe, sender, INIT_TTL, -1);
    }

    public static ScamplonMessage forward(Node sender, ScamplonMessage message) {
        ScamplonMessage result = new ScamplonMessage(Type.Forward, sender, message.ttl -1, -1);
        switch (message.type) {
            case Subscribe:
                result.subscriber = message.sender;
                break;
            case Forward:
                result.subscriber = message.subscriber;
                break;
            default:
                throw new RuntimeException("wrong message type");
        }
        return result;
    }

    public static ScamplonMessage shuffleWithSecret(Node sender, List<PartialView.Entry> send, final int secret) {
        ScamplonMessage message = new ScamplonMessage(Type.Shuffle, sender, -1, secret);
        message.a = send;
        return message;
    }

    public static ScamplonMessage shuffleResponse(Node sender, List<PartialView.Entry> send, ScamplonMessage shuffle) {
        if (shuffle.sender.getID() == sender.getID() || shuffle.type != Type.Shuffle) {
            throw new RuntimeException("wrong message");
        }
        ScamplonMessage message = new ScamplonMessage(Type.ShuffleResponse, sender, -1, shuffle.secret);
        message.a = send;
        message.b = shuffle.a;
        return message;
    }

}
