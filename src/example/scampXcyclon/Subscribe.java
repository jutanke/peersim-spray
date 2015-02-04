package example.scampXcyclon;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by julian on 2/4/15.
 */
public class Subscribe implements Control {

    private static final String PAR_PROT = "protocol";
    private static final String SCAMP_PROT = "0";

    private final int protocolID;
    public static int pid;

    public Subscribe(String prefix) {
        protocolID = Configuration.getInt(prefix + "." + PAR_PROT);
        pid = Configuration.lookupPid(SCAMP_PROT);
    }

    @Override
    public boolean execute() {

        for (int i = 1; i < Network.size(); i++) {
            Node n = Network.get(i);
            Scamplon.subscribe(n);
        }

        return false;
    }
}
