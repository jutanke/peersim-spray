package example.cyclon;

import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 26/01/15.
 */
public class CyclonMessage {

    public enum Type {
        Shuffle,
        ShuffleResponse
    }

    public Type type;
    public Node sender;
    public List<CyclonEntry> list;
    public List<CyclonEntry> temp;

    public CyclonMessage(Node n, Type t, List<CyclonEntry> list, List<CyclonEntry> temp) {
        this.type = t;
        this.sender = n;
        this.list = list;
        this.temp = temp;
    }

}
