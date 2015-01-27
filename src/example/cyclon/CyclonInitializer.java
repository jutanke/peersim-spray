package example.cyclon;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

/**
 * Created by julian on 27/01/15.
 */
public class CyclonInitializer implements NodeInitializer {

    private static final String CYCLON_PROT = "lnk";

    private String prefix;
    private final int pid;

    public CyclonInitializer(String prefix) {
        this.pid = Configuration.lookupPid(CYCLON_PROT);
        this.prefix = prefix;
    }

    @Override
    public void initialize(Node n) {

        int pos = 0;
        if (Network.size() == 1) {

        } else if (Network.size() > 1) {
            pos = Network.size()-1;
        }
        CyclonSimple cyclon = (CyclonSimple) Network.get(pos).getProtocol(pid);
        cyclon.addNeighbor(n);
        System.err.println("connect " + n.getID() + " to " + Network.get(Network.size() - 1).getID());



    }
}
