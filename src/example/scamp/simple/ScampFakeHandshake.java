package example.scamp.simple;

import example.scamp.ScampProtocol;
import example.scamp.ScampWithView;
import example.scamp.messaging.*;
import example.scamp.messaging.ScampMessage;
import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import sun.org.mozilla.javascript.ast.Loop;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by julian on 2/3/15.
 */
public class ScampFakeHandshake extends ScampWithView {

    // ===================================================
    // E N T I T Y
    // ===================================================

    public ScampFakeHandshake(String s) {
        super(s);
    }

    // ===================================================
    // P U B L I C  I N T E R F A C E
    // ===================================================

    @Override
    public void subRejoin(Node me, long newBirthDate) {

        this.inView.clear();

        ScampMessage message = ScampMessage.createKeepAlive(me, birthDate);
        for (Node n : this.partialView.list()) {
            send(me, n, message);
        }

        if (this.degree() > 0) {
            this.join(me, this.partialView.get(0).node); // does not matter thanks to indirection
        }
    }

    @Override
    public void subProcessEvent(Node node, ScampMessage message) {

        switch (message.type) {
            case ForwardSubscription:
                doSubscribe(node, message);
                break;
            case Loop:
                if (message.refreshLoopCounter()) {
                    // END LOOP
                    Node subscriber, contact;
                    ScampFakeHandshake ppS, ppC;
                    switch (message.topic) {
                        case Indirection:
                            subscriber = message.payload;
                            contact = Network.get(CDState.r.nextInt(Network.size()));
                            while (contact.getID() == subscriber.getID()) {
                                contact = Network.get(CDState.r.nextInt(Network.size()));
                            }
                            print("Indirection done => connect subscription " + subscriber.getID() +
                                    " to contact " + contact.getID());
                            ScampMessage backtracking = ScampMessage.smallLoop(
                                    contact, ScampMessage.LoopTopic.BacktrackConnection, subscriber);
                            this.send(node, node, backtracking);
                            break;
                        case BacktrackConnection:
                            subscriber = message.payload;
                            contact = message.sender;
                            System.err.println("== Subscribe " +
                                    subscriber.getID() + " to contact " + contact.getID());
                            subscribe(contact, subscriber);
                            break;
                        case AcceptSubscription:
                            subscriber = message.payload;
                            contact = message.sender;
                            print("ACCEPT: s:" + subscriber.getID() + " @" + contact.getID());
                            ppS = (ScampFakeHandshake) subscriber.getProtocol(pid);
                            ppC = (ScampFakeHandshake) contact.getProtocol(pid);
                            ppS.addToInView(contact);
                            ppC.addNeighbor(subscriber);
                            break;
                        default:
                            throw new NotImplementedException();
                    }
                } else {
                    // KEEP LOOPING
                    print("pending message: " + message.debugLoop());
                    if (message.keepLooping()) this.send(node, node, message);
                }
                break;
            default:
                throw new NotImplementedException();
        }

    }

    @Override
    public void subNextCycle(Node node) {
        // CHEATING!

        long now = CommonState.getTime();
        if (now > 20 && ____C_H_E_A_T_I_N_G____ && this.inView.length() == 0 && this.partialView.length() == 0) {
            System.err.println("============ CHEATING =========== @" + node.getID());
            Node contact = Network.get(CDState.r.nextInt(Network.size()));
            ScampProtocol.subscribe(contact, node);
            CHEAT_COUNT += 1;
        }
    }

    @Override
    public void handleSubscription(Node n, example.scamp.ScampMessage m) {
        throw new NotImplementedException();
    }

    @Override
    public void join(Node me, Node subscriber) {
        indirection(me, subscriber);
    }

    @Override
    public void unsubscribe(Node me) {

    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================

    private void acceptSubscription(Node acceptor, Node subscriber) {
        if (acceptor.getID() == subscriber.getID()) {
            throw new RuntimeException("@" + acceptor.getID() + "Try to accept myself as subscription");
        } else {
            print("Accept [OUT] " + subscriber.getID() + " -> " + acceptor.getID());
            ScampMessage accept = ScampMessage.smallLoop(
                    acceptor, ScampMessage.LoopTopic.AcceptSubscription, subscriber);
            this.send(acceptor, acceptor, accept);
        }
    }

    private void indirection(Node n, Node s) {
        print("Indirection s:" + s.getID() + " -> " + n.getID());
        ScampFakeHandshake contact = (ScampFakeHandshake) n.getProtocol(pid);
        ScampFakeHandshake subscriber = (ScampFakeHandshake) s.getProtocol(pid);
        ScampMessage loop = ScampMessage.smallLoop(n, ScampMessage.LoopTopic.Indirection, s);
        this.send(n, n, loop);
    }

    public static void subscribe(Node n, Node s) {
        print("Subscribe s:" + s.getID() + " -> " + n.getID());
        ScampFakeHandshake subscriber, contact;

        subscriber = (ScampFakeHandshake) s.getProtocol(pid);
        contact = (ScampFakeHandshake) n.getProtocol(pid);
        contact.addToInView(s);
        subscriber.addNeighbor(n);
        print("s:" + s.getID() + " -> " + n.getID());

        ScampMessage forward = ScampMessage.createForwardSubscription(n, s);

        if (contact.degree() == 0) {
            System.err.println("SCAMP: zero degree contact node " + s.getID() + " -> " + n.getID());
            doSubscribe(n, forward);
        } else {
            for (int i = 0; i < contact.partialView.length(); ++i) {
                doSubscribe(contact.getNeighbor(i), forward);
            }
            for (int i = 0; i < c; ++i) {
                doSubscribe(contact.getNeighbor(CDState.r.nextInt(contact.degree())), forward);
            }
        }
    }


    private static void doSubscribe(final Node n, ScampMessage forward) {
        if (!forward.isExpired()) {
            Node s = forward.payload;
            ScampFakeHandshake pp = (ScampFakeHandshake) n.getProtocol(pid);
            if (forward.ttl > 100) print("DoSub: @" + n.getID() + " s:" + s.getID() + " @" + pp.debug());
            if (pp.p() && !pp.contains(s) && n.getID() != s.getID()) {
                //throw new NotImplementedException();
                pp.acceptSubscription(n, s);
            } else if (pp.degree() > 0) {
                Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
                if (forward.ttl > 100) print("Forward s:" + s.getID() + " from " + n.getID() + " to " + forwardTarget.getID() + " ttl:" + forward.ttl);
                forward = ScampMessage.updateForwardSubscription(n, forward);
                pp.send(n, forwardTarget, forward);
            } else {
                //..
            }
        }
    }

}
