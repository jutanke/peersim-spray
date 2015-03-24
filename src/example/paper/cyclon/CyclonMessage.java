package example.paper.cyclon;

import peersim.core.CommonState;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 3/14/15.
 */
public class CyclonMessage {

    public enum Type {
        Shuffle,
        ShuffleResponse,
        Rollback
    }

    public final Type type;
    public final Node sender;
    public final long creationDate;
    public final int secret;

    public List<CyclonEntry> send;
    public List<CyclonEntry> received;

    public CyclonMessage(Type type, Node sender, int secret) {
        this.type = type;
        this.sender = sender;
        this.creationDate = CommonState.getTime();
        this.secret = secret;
    }

    public CyclonMessage(Type type, Node sender) {
        this(type, sender, -1);
    }

    @Override
    public String toString() {
        return "{" + this.type.toString() + " | sender:" + this.sender.getID() + " | age:" + (CommonState.getTime() - this.creationDate) +"}";
    }

    // ============================================================

    public static CyclonMessage shuffle(Node sender, List<CyclonEntry> send) {
        CyclonMessage message = new CyclonMessage(Type.Shuffle, sender);
        message.send = send;
        return message;
    }

    public static CyclonMessage shuffleResponse(Node sender, List<CyclonEntry> send, CyclonMessage msg) {
        CyclonMessage message = new CyclonMessage(Type.ShuffleResponse, sender, msg.secret);
        message.send = send;
        if (msg.type != Type.Shuffle || msg.sender.getID() == sender.getID()) {
            throw new RuntimeException("Wrong message");
        }
        message.received = msg.send;
        return message;
    }

    public static CyclonMessage shuffleWithSecret(Node sender, List<CyclonEntry> send, final int secret) {
        CyclonMessage message = new CyclonMessage(Type.Shuffle, sender, secret);
        message.send = send;
        return message;
    }

}
