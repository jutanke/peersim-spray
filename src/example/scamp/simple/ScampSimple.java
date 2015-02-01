package example.scamp.simple;

import example.scamp.ScampProtocol;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    public Object clone() {
        ScampSimple scamp = null;
        scamp = (ScampSimple) super.clone();
        this.deleteList = new ArrayList<Node>();
        return scamp;
    }

    // =================== public =========================================
    // ====================================================================

    @Override
    public void subNextCycle(Node node, int protocolID) {

        //System.err.println("+++++++++++ CYCLE ++++++++++++ (" + node.getID());

        // remove all expired nodes from our partial view
        this.deleteList.clear();
        for (Node n : this.partialView.list()) {
            ScampSimple scamp = (ScampSimple) n.getProtocol(pid);
            if (scamp.isExpired()) {
                this.deleteList.add(n);
            }
        }
        for (Node n : this.deleteList) {
            System.out.println("@" + node.getID() + ":remove from partial view:" + n.getID());
            this.partialView.del(n);
        }
        this.deleteList.clear();


        // lease (re-subscription)
        if (this.isExpired()) {
            System.err.println( node.getID() + " is expired!");
            this.unsubscribe(node);
        }

    }

    @Override
    public void subProcessEvent(Node node, int pid, ScampMessage message) {

        //System.err.println(node.getID() + "=>:" + message);

        message.reduceTTL();  // handle ttl
        if (message.isValid()) {  // else the message just gets discarded
            switch (message.type) {
                case Subscribe:
                    this.subscriptionManagement(node, message.subscriber);
                    break;
                case Unsubscribe:
                    break;
                case ForwardSubscription:
                    handleForwardedSubscription(node, message);
                    break;
                case AcceptedSubscription:
                    if (this.inView.contains(message.sender)) {
                        System.out.println("must not happen..");
                    }
                    this.addToInView(message.sender);
                    break;
                case RequestContact:
                    break;
            }
        }

    }

    @Override
    public void requestConnection(Node me, Node subscriber) {

        // TODO figure out how to calculate the {proportionality constant}
        int counter = 2 * (int) Math.ceil(Math.log(this.partialView.length()));

        //TODO keep going!
        throw new NotImplementedException();
    }

    // =================== PUBLIC SCAMP ===================================
    // ====================================================================


    /**
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
     * @param me
     * @param subscriber
     */
    private void subscriptionManagement(Node me, Node subscriber) {

        // we must put the subscriber into our inview
        this.addToInView(subscriber);

        ScampMessage message = ScampMessage.forwardSubscription(me, subscriber);

        for (Node e : this.partialView.list()) {
            forwardSubscription(me, e, message);
        }
        for (int j = 0; j < c; j++) {
            Node n = randomOutNode();
            forwardSubscription(me, n, message);
        }
    }

    /**
     * @param me
     * @param message
     */
    private void handleForwardedSubscription(Node me, ScampMessage message) {
        if (p() && !this.partialView.contains(message.subscriber) && me.getID() != message.subscriber.getID()) {
            this.acceptSubscription(me, message.subscriber);
        } else {
            Node n = randomOutNode();
            if (n != null) {
                forwardSubscription(me, n, message);
            } else {
                System.out.println("DEAD END for " + message.subscriber.getID() + " @" + me.getID());
            }

        }
    }

    //private void forwardSubscription(Node sender, Node receiver, Node subscriber) {
    private void forwardSubscription(Node me, Node receiver, ScampMessage message) {
        //if (receiver.getID() != message.subscriber.getID()) {
        //ScampMessage message = new ScampMessage(sender, ScampMessage.Type.ForwardSubscription, subscriber);
        //Transport tr = (Transport) sender.getProtocol(tid);
        //tr.send(sender, receiver, message, pid);
        message = new ScampMessage(me, message);
        send(me, receiver, message);
        //} else {
        //    System.err.println("CANNOT FORWARD OWN SUBSCRIPTION!" + receiver.getID() + "-" + message.subscriber.getID());

//            ScampProtocol pp = (ScampProtocol) me.getProtocol(pid);
        //          System.err.println(me.getID() + " -> " + pp);

        //}

    }

    /**
     * @param sender
     * @param subscriber
     */
    private void acceptSubscription(Node sender, Node subscriber) {
        if (sender.getID() == subscriber.getID()) {
            throw new RuntimeException("MUST NOT SUBSCRIBE TO MYSELF!");
        }
        this.partialView.add(subscriber);
        ScampMessage message = new ScampMessage(sender, ScampMessage.Type.AcceptedSubscription, null);
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, subscriber, message, pid);
    }
}
