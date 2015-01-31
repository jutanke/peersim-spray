package example.scamp.simple;

import example.scamp.Scamp;
import example.scamp.ScampProtocol;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.*;

/**
 * Created by julian on 29/01/15.
 */
public class ScampSimple extends ScampProtocol {


    List<Long> deleteList;

    // ===================== initialization ================================
    // =====================================================================

    public ScampSimple(String n) {
        super(n);
        this.deleteList = new ArrayList<Long>();
    }

    @Override
    public Object clone(){
        ScampSimple scamp = null;
        scamp = (ScampSimple) super.clone();
        this.deleteList = new ArrayList<Long>();
        return scamp;
    }

    // =================== public =========================================
    // ====================================================================

    @Override
    public void nextCycle(Node node, int protocolID) {

        // lease (re-subscription)
        if (this.isExpired()) {
            this.unsubscribe(node);
        }

        // remove all expired nodes from our partial view
        this.deleteList.clear();
        for (Node n : this.outView.values()) {
            ScampSimple scamp = (ScampSimple) n.getProtocol(pid);
            if (scamp.isExpired()) {
                this.deleteList.add(n.getID());
            }
        }
        for (long id : this.deleteList) {
            this.outView.remove(id);
        }
        this.deleteList.clear();


    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

        ScampMessage message = (ScampMessage) event;

        message.reduceTTL();  // handle ttl
        if (message.isValid()) {  // else the message just gets discarded
            switch (message.type) {
                case Subscribe:
                    this.subscriptionManagement(node, message.subscriber);
                    break;
                case Unsubscribe:
                    break;
                case ForwardSubscription:
                    handleForwardedSubscription(node, message.subscriber);
                    break;
                case AcceptedSubscription:
                    if (this.inView.containsKey(message.sender.getID())) throw new RuntimeException("QNOPE");
                    this.inView.put(message.sender.getID(), message.sender);
                    break;
            }
        }

    }

    // =================== PUBLIC SCAMP ===================================
    // ====================================================================

    /**
     * this is the first step to enter a network
     * @param contact
     */
    public void join(Node me, Node contact) {
        this.birthDate = CDState.getCycle();
        if (contact != null) {
            System.err.println("JOIN " + me.getID() + " to contact " + contact.getID());
            //this.inView.clear();
            //this.outView.clear();
            //this.outView.put(contact.getID(), contact);
            this.addNeighbor(contact);
            ScampMessage message = new ScampMessage(me, ScampMessage.Type.Subscribe, me);
            Transport tr = (Transport) me.getProtocol(tid);
            tr.send(me, contact, message, pid);
        } else {
            System.err.println("JOIN-ERROR:COULD NOT FIND A CONTACT FOR NODE " + me.getID());
        }
    }

    /**
     *
     * @param me
     */
    public void unsubscribe(Node me) {

        // select random entry point
        Node n = randomOutNode();

        join(me, n);

        // CHECK


    }



    // =================== helper =========================================
    // ====================================================================



    // =================== event handler ==================================
    // ====================================================================


    /**
     *
     * @param me
     * @param subscriber
     */
    private void subscriptionManagement(Node me, Node subscriber) {
        for (Node e : this.outView.values()) {
            forwardSubscription(me, e, subscriber);
        }
        for (int j = 0; j < c; j++) {
            Node n = randomOutNode();
            forwardSubscription(me, n, subscriber);
        }
    }

    /**
     *
     * @param me
     * @param subscriber
     */
    private void handleForwardedSubscription(Node me, Node subscriber) {
        if (p() && !this.outView.containsKey(subscriber.getID())) {
            this.outView.put(subscriber.getID(), subscriber);
            this.acceptSubscription(me, subscriber);
        } else {
            Node n = randomOutNode();
            forwardSubscription(me, n, subscriber);
        }
    }

    /**
     * forward the subscription
     * @param sender
     * @param receiver
     * @param subscriber
     */
    private void forwardSubscription(Node sender, Node receiver, Node subscriber) {
        if (receiver.getID() != subscriber.getID()) {
            ScampMessage message = new ScampMessage(sender, ScampMessage.Type.ForwardSubscription, subscriber);
            Transport tr = (Transport) sender.getProtocol(tid);
            tr.send(sender, receiver, message, pid);
        } else {
            System.err.println("CANNOT FORWARD OWN SUBSCRIPTION!");
        }
    }

    /**
     *
     * @param sender
     * @param subscriber
     */
    private void acceptSubscription(Node sender, Node subscriber) {
        if (sender.getID() == subscriber.getID()) {
            throw new RuntimeException("MUST NOT SUBSCRIBE TO MYSELF!");
        }
        ScampMessage message = new ScampMessage(sender, ScampMessage.Type.AcceptedSubscription, null);
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, subscriber, message, pid);
    }
}
