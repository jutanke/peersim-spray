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




    // ===================== initialization ================================
    // =====================================================================

    public ScampSimple(String n) {
        super(n);
    }

    // =================== public =========================================
    // ====================================================================

    @Override
    public void subNextCycle(Node node, int protocolID) {

        //System.err.println("+++++++++++ CYCLE ++++++++++++ (" + node.getID());

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
                case RequestContact:
                    break;
                case HandleUnsubscribeIn: // handles the sending nodes in-view
                    this.partialView.del(message.sender);
                    if (!this.partialView.contains(message.replacer) &&
                            message.replacer.getID() != node.getID()) {
                        this.acceptSubscription(node, message.replacer);
                    }
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


    @Override
    public void lease(Node me) {

        System.err.println("@" + me.getID() + " lease..");

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
        //this.addToInView(subscriber);

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

    private void forwardSubscription(Node me, Node receiver, ScampMessage message) {

        message = new ScampMessage(me, message);
        send(me, receiver, message);

    }
}
