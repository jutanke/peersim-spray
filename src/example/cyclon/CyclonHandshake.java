package example.cyclon;

import peersim.core.Node;
import peersim.transport.Transport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by julian on 27/01/15.
 */
public class CyclonHandshake extends CyclonSimple {

    // ======================================================================
    // P R O P E R T I E S
    // ======================================================================
    public CyclonHandshake(int size, int l) {
        super(size, l);
    }

    private Map<Long, CyclonEntry> pending = new HashMap<Long, CyclonEntry>();

    // ======================================================================
    // P U B L I C  I N T E R F A C E
    // ======================================================================

    @Override
    public void processEvent(Node node, int pid, Object event) {
        super.processEvent(node, pid, event);

        CyclonMessage message = (CyclonMessage) event;


        // A - B - C
        CyclonEntry dest;
        switch (message.type) {
            case RequestOffer:
                // A -> B
                dest = get(message.answerNode.n.getID());
                if (dest == null) {
                    send(node, message.offerNode.n, CyclonMessage.Type.CouldNotLink, message.offerNode, message.answerNode, pid);
                } else {
                    send(node, message.answerNode.n, CyclonMessage.Type.RequestAnswer, message.offerNode, message.answerNode, pid);
                }
                break;
            case RequestAnswer:
                // directly accept!
                // B -> C
                pending.put(message.offerNode.n.getID(), message.offerNode);
                send(node, message.sender, CyclonMessage.Type.GiveAnswer, message.offerNode, message.answerNode, pid);
                break;
            case GiveAnswer:
                // C -> B
                dest = get(message.offerNode.n.getID());
                if (dest == null) {
                    send(node, message.sender, CyclonMessage.Type.CouldNotLink, message.offerNode, message.answerNode, pid);
                } else {
                    send(node, message.offerNode.n, CyclonMessage.Type.DeliverAnswer, message.offerNode, message.answerNode, pid);
                }
                break;
            case DeliverAnswer:
                // B -> A
                /*if (node.getID() == message.offerNode.n.getID()) {
                    Insert(message.answerNode);
                } else if (node.getID() == message.answerNode.n.getID()) {
                    Insert(message.offerNode);
                } else {
                    throw new RuntimeException("Never.. " + message.offerNode.n.getID() +
                        " and " + message.answerNode.n.getID() + " at " + node.getID());
                }*/
                Insert(message.answerNode);
                send(node, message.answerNode.n, CyclonMessage.Type.Connected, message.offerNode, message.answerNode, pid);
                break;
            case Connected:
                // A -> C
                Insert(message.offerNode);
                break;
            case CouldNotLink:
                //A -> B -> ??
                System.err.println("(" + node.getID() + ") could not link " + message.offerNode.n.getID() +
                    "-" + message.answerNode.n.getID());
                break;
        }
    }

    // ======================================================================
    // P R I V A T E  I N T E R F A C E
    // ======================================================================

    public void attemptToInsert(CyclonEntry e) {
        
    }

    private void Insert(CyclonEntry n) {
        if (cache.size() >= size) {
            Node oldest = selectOldest();
            this.cache = delete(this.cache, oldest);
        }
        cache.add(n);
    }

    private CyclonEntry get(long id) {
        for (CyclonEntry e : this.cache) {
            if (e.n.getID() == id) {
                return e;
            }
        }
        return null;
    }

    protected boolean send(Node sender,
                           Node receiver,
                           CyclonMessage.Type type,
                           CyclonEntry offer,
                           CyclonEntry answer,
                           int protocolID) {
        if (sender.getID() != receiver.getID()) {
            CyclonMessage message = new CyclonMessage(sender, type, offer, answer);
            Transport tr = (Transport) sender.getProtocol(tid);
            tr.send(sender, receiver, message, protocolID);
            return true;
        }
        System.err.println("Dublicate: " + sender.getID() + " - " + receiver.getID());
        return false;
    }

}