package example.scamp.simple;

import example.scamp.ScampProtocol;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import java.util.*;

/**
 * Created by julian on 29/01/15.
 */
public class ScampSimple extends ScampProtocol {


    List<Node> deleteList;

    // ===================== initialization ================================
    // =====================================================================

    public ScampSimple(String n) {
        super(n);
        this.deleteList = new ArrayList<Node>();
    }

    @Override
    public Object clone(){
        ScampSimple scamp = null;
        scamp = (ScampSimple) super.clone();
        this.deleteList = new ArrayList<Node>();
        return scamp;
    }

    // =================== public =========================================
    // ====================================================================

    @Override
    public void subNextCycle(Node node, int protocolID) {

        System.err.println("+++++++++++ CYCLE ++++++++++++ (" + node.getID());

        // lease (re-subscription)
        if (this.isExpired()) {
            this.unsubscribe(node);
        }

        // remove all expired nodes from our partial view
        this.deleteList.clear();
        for (Node n : this.partialView.list()) {
            ScampSimple scamp = (ScampSimple) n.getProtocol(pid);
            if (scamp.isExpired()) {
                this.deleteList.add(n);
            }
        }
        for (Node n : this.deleteList) {
            this.partialView.del(n);
        }
        this.deleteList.clear();


    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

        ScampMessage message = (ScampMessage) event;

        System.err.println(node.getID() + "=>:" + message);

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
                    if (this.inView.contains(message.sender)) throw new RuntimeException("QNOPE");
                    this.inView.add(message.sender);
                    break;
            }
        }

    }

    // =================== PUBLIC SCAMP ===================================
    // ====================================================================


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

        // we must put the subscriber into our inview
        this.addToInView(subscriber);

        for (Node e : this.partialView.list()) {
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
        if (p() && !this.partialView.contains(subscriber)) {
            this.partialView.add(subscriber);
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
