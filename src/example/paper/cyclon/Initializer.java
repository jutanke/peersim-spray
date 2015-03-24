package example.paper.cyclon;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by julian on 3/15/15.
 */
public class Initializer implements Control {

    private static final String CYCLON_PROT = "lnk";

    private final int pid;

    public Initializer(String prefix) {
        this.pid = Configuration.lookupPid(CYCLON_PROT);
    }

    @Override
    public boolean execute() {

        // RANDOM
        Node n0 = Network.get(0);
        Node n1 = Network.get(1);

        CyclonProtocol c = (CyclonProtocol) n0.getProtocol(pid);
        c.addNeighbor(n1);

        for (int i = 2; i < Network.size(); i++) {
            Node me = Network.get(i);
            int o = CDState.r.nextInt(i-1);

            Node other = Network.get(o);

            CyclonProtocol pp = (CyclonProtocol) me.getProtocol(pid);
            pp.addNeighbor(other);

            //System.err.println("init " + o + " -> " + i );
            //Cyclon other = (Cyclon) Network.get(o).getProtocol(pid);
            //other.addNeighbor(me);
        }

        return false;
    }
}
