package example.webrtc.cyclon2;

import peersim.core.Node;

import java.util.Comparator;
import java.util.List;

/**
 * Created by julian on 25/01/15.
 */
public class CyclonMessage {

    public Node node;
    public List<CyclonEntry> list;
    public boolean isResuest;

    public List<CyclonEntry> receivedList;

    public CyclonMessage(Node node, List<CyclonEntry> list, boolean isRequest, List<CyclonEntry> receivedList)
    {
        this.node = node;
        this.list = list;
        this.isResuest = isRequest;
        this.receivedList = receivedList;
    }

}
