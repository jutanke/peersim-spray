package example.paper.cyclon;

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

    public List<CyclonEntry> send;
    public List<CyclonEntry> received;

    public CyclonMessage(Type type, Node sender) {
        this.type = type;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "{" + this.type.toString() + " | sender:" + this.sender.getID() + "}";
    }

    // ============================================================

    public static CyclonMessage shuffle(Node sender, List<CyclonEntry> send) {
        CyclonMessage message = new CyclonMessage(Type.Shuffle, sender);
        message.send = send;
        return message;
    }

    public static CyclonMessage shuffleResponse(Node sender, List<CyclonEntry> send, CyclonMessage msg) {
        CyclonMessage message = new CyclonMessage(Type.ShuffleResponse, sender);
        message.send = send;
        if (msg.type != Type.Shuffle || msg.sender.getID() == sender.getID()) {
            throw new RuntimeException("Wrong message");
        }
        message.received = msg.send;
        return message;
    }

}
