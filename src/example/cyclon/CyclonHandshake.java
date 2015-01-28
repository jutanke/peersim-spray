package example.cyclon;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by julian on 27/01/15.
 */
public class CyclonHandshake extends CyclonSimple {

    private static final String CYCLON_PROT = "lnk";

    // ======================================================================
    // P R O P E R T I E S
    // ======================================================================
    public CyclonHandshake(int size, int l, int pid) {
        super(size, l);
        this.pid = pid;
    }

    private final int pid;

    public CyclonHandshake(String n) {
        super(n);
        this.pid = Configuration.lookupPid(CYCLON_PROT);
    }

    // ======================================================================
    // P U B L I C  I N T E R F A C E
    // ======================================================================

    @Override
    public void processEvent(Node node, int pid, Object event) {
        //super.processEvent(node, pid, event);

        CyclonMessage message = (CyclonMessage) event;
        List<CyclonEntry> received = null;
        List<CyclonEntry> nodesToSend = null;

        // A - B - C
        CyclonEntry dest;
        switch (message.type) {

            case Shuffle:

                Node p = message.sender;

                received = message.list;
                nodesToSend = selectNeighbors(l);
                //System.err.println("++++++ A ++++++");
                this.cache = merge(node, p, this.cache, clone(received), clone(nodesToSend));

                send(node, p, CyclonMessage.Type.ShuffleResponse, nodesToSend, received, pid);

                break;
            case ShuffleResponse:

                received = message.list;
                nodesToSend = message.temp;
                //System.err.println("++++++ B ++++++");
                this.cache = merge(
                        node,
                        message.sender,
                        this.cache,
                        clone(received),
                        clone(nodesToSend));

                break;
            case RequestOffer:
                // A -> B
                dest = get(message.answerNode.n.getID());
                if (dest == null) {
                    //System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAA");
                    send(node, message.offerNode, CyclonMessage.Type.CouldNotLink, message.offerNode, message.answerNode, pid);
                } else {
                    send(node, message.answerNode.n, CyclonMessage.Type.RequestAnswer, message.offerNode, message.answerNode, pid);
                }
                break;
            case RequestAnswer:
                // directly accept!
                // B -> C
                send(node, message.sender, CyclonMessage.Type.GiveAnswer, message.offerNode, message.answerNode, pid);
                break;
            case GiveAnswer:
                // C -> B
                dest = get(message.offerNode.getID());
                if (dest == null) {
                    //System.err.println("BBBBBBBBBBBBBBBBBBBBBB");
                    send(node, message.sender, CyclonMessage.Type.CouldNotLink, message.offerNode, message.answerNode, pid);
                } else {
                    send(node, message.offerNode, CyclonMessage.Type.DeliverAnswer, message.offerNode, message.answerNode, pid);
                }
                break;
            case DeliverAnswer:
                // B -> A
                Insert(message.answerNode);
                send(node, message.answerNode.n, CyclonMessage.Type.Connected, message.offerNode, message.answerNode, pid);
                break;
            case Connected:
                // A -> C
                //Insert(message.offerNode);
                //System.err.println("CONNECTED!");
                break;
            case CouldNotLink:
                //A -> B -> ??
                //System.err.println("(" + message.sender.getID()+ ") could not link " + message.offerNode.getID() +
                //        "-" + message.answerNode.n.getID());
                break;
        }
    }

    // ======================================================================
    // P R I V A T E  I N T E R F A C E
    // ======================================================================


    public List<CyclonEntry> merge(Node self, Node sender, List<CyclonEntry> cache, List<CyclonEntry> received, List<CyclonEntry> sent) {

        // Discard entries pointing at P and entries already contained in P`s cache
        received = delete(received, self);
        received = discard(received, cache);

        // Update P`s cache, to include all remaining entries, by firstly using empty cache slots (if any),
        // and secondly replacing entries among the ones sent to Q
        cache = discard(cache, sent);
        sent = delete(sent, self); // clean

        // because auf async we might have different entries in {sent} and {cache}...
        sent = discard(sent, received);

        Collections.sort(received, new CyclonEntry());

        int include = Math.min(size - cache.size(), received.size());
        for (int i = 0; i < include; i++){
            CyclonEntry e = received.get(i);
            if (e.n.getID() == sender.getID()) {
                cache.add(received.get(i));
            } else {
                send(self, sender, CyclonMessage.Type.RequestOffer, self, received.get(i), pid);
            }
        }

        Collections.sort(sent, new CyclonEntry());

        while (cache.size() < size && sent.size() > 0) {
            cache.add(popSmallest(sent));
        }

        return cache;
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
                           Node offer,
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