package example.paper.scamp;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by julian on 3/17/15.
 */
public class Initializer implements Control {

    private static final String SCAMP_PROT = "0";

    private final int pid;

    public Initializer(String prefix) {
        this.pid = Configuration.lookupPid(SCAMP_PROT);
    }

    @Override
    public boolean execute() {

        Node n0 = Network.get(0);
        Scamp.initialize(n0);

        Node n1 = Network.get(1);
        Scamp.subscribe(n1, 0);

        for(int i = 2; i < Network.size(); i++) {
            Node me = Network.get(i);
            //int o = CDState.r.nextInt(i-1);
            Scamp.subscribe(me, i-1);
        }

        return false;
    }
}
