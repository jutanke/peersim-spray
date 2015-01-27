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

        if (Network.size() == 1) {

            // always add at the first element
            CyclonSimple cyclon = (CyclonSimple) Network.get(0).getProtocol(pid);
            cyclon.addNeighbor(n);

        }

        if (Network.size() > 1) {

            int pos = CommonState.r.nextInt(Network.size() - 2) + 1; // prevent 1 to be selected
            CyclonSimple cyclon = (CyclonSimple) Network.get(pos).getProtocol(pid);
            cyclon.addNeighbor(n);

        }

        if (Network.size() > 2) {

            int pos = CommonState.r.nextInt(Network.size() - 3) + 2; // prevent 1 to be selected
            CyclonSimple cyclon = (CyclonSimple) Network.get(pos).getProtocol(pid);
            cyclon.addNeighbor(n);

        }


    }
}
