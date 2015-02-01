package example.scamp;

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

    /**
     * The protocol we want to wire
     */
    private final int protocolID;

    public Subscribe(String prefix) {
        protocolID = Configuration.getInt(prefix + "." + PAR_PROT);
    }


    @Override
    public boolean execute() {

        line();

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
            Scamp.subscribe(contact, me);
            contact = me;
        }
    }
}
