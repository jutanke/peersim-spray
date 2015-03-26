package example.paper.scamplon;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by julian on 3/25/15.
 */
public class Initializer implements Control {

    private static final String SCAMPLON_PROT = "0";

    private final int pid;

    public Initializer(String prefix) {
        this.pid = Configuration.lookupPid(SCAMPLON_PROT);
    }

    @Override
    public boolean execute() {

        Node n0 = Network.get(0);
        Node n1 = Network.get(1);

        Scamplon.subscribe(n1, n0);

        for (int i = 2; i < Network.size(); i++) {
            final Node contact = Network.get(CommonState.r.nextInt(i-1));
            final Node subscriber = Network.get(i);
            Scamplon.subscribe(subscriber, contact);
        }

        return false;
    }
}
