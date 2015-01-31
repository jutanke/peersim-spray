package example.scamp.simple;

import example.scamp.ScampProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

/**
 * Created by julian on 31/01/15.
 */
public class ScampSubscribe implements NodeInitializer, Control {

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

    public ScampSubscribe(String prefix) {
        protocolID = Configuration.getInt(prefix + "." + PAR_PROT);
    }

    @Override
    public void initialize(Node n) {

        if (true) throw new RuntimeException("IS NOT IN USE!");

        if (Network.size() == 0) return;

        Node contact = Network.get(CommonState.r.nextInt(Network.size()));

        ScampSimple scamp = (ScampSimple) n.getProtocol(protocolID);

        System.err.println("TRY TO JOIN: " + n.getID());
        scamp.join(n, contact);

    }

    @Override
    public boolean execute() {

        star();

        return false;
    }


    private void star() {
        System.err.println("++++++++++++++ interconnect nodes as STAR! ++++++++++++++");

        Node contact = Network.get(0);
        for (int i = 1; i < Network.size(); i++) {

            Node me = Network.get(i);
            ScampProtocol current = get(i);
            current.join(me, contact);
        }
    }

    private ScampProtocol get(final int i) {
        return (ScampProtocol) Network.get(i).getProtocol(protocolID);
    }
}
