package example.scamp;

import example.scamp.orig.*;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by julian on 01/02/15.
 */
public class Subscribe implements NodeInitializer, Control {

    /**
     * The protocol to operate on. It has to be a scamp protocol.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";
    private static final String SCAMP_PROT = "0";

    /**
     * The protocol we want to wire
     */
    private final int protocolID;
    public static int pid;

    public Subscribe(String prefix) {
        protocolID = Configuration.getInt(prefix + "." + PAR_PROT);
        pid = Configuration.lookupPid(SCAMP_PROT);
    }


    @Override
    public boolean execute() {

        random();

        return false;
    }

    @Override
    public void initialize(Node n) {
        throw new NotImplementedException();
    }

    private void line() {
        System.err.println("++++++++++++++ interconnect nodes as LINE! ++++++++++++++");

        Node contact = Network.get(0);
        for (int i = 1; i < Network.size(); i++) {
            Node me = Network.get(i);
            ScampProtocol.subscribe(contact, me);
            contact = me;
        }
    }

    private void forcedLine() {
        System.err.println("++++++++++++++ interconnect nodes as forced LINE! ++++++++++++++");

        Node contact = Network.get(0);
        for (int i = 1; i < Network.size(); i++) {
            Node me = Network.get(i);

            ScampWithView c = (ScampWithView) contact.getProtocol(pid);
            ScampWithView m = (ScampWithView) me.getProtocol(pid);

            c.addToOutView(me);
            m.addToInView(contact);

            c.addToInView(me);
            m.addToOutView(contact);

            contact = me;
        }

    }

    private void random() {
        System.err.println("++++++++++++++ interconnect nodes as randomly! ++++++++++++++");

        Node n0 = Network.get(0);
        Node n1 = Network.get(1);
        ScampProtocol.subscribe(n0, n1);

        for (int i = 2; i < Network.size(); i++) {
            Node me = Network.get(i);
            int o = CDState.r.nextInt(i-1);
            Node other = Network.get(o);

            ScampProtocol.subscribe(other, me);

        }

    }
}
