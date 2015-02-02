package example.cyclon;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;

/**
 * Created by julian on 27/01/15.
 */
public class CyclonInitializer implements Control, NodeInitializer {

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

    @Override
    public boolean execute() {
        random();
        return false;
    }


    private void random() {
        System.err.println("++++++++++++++ interconnect nodes as randomly! ++++++++++++++");

        Node n0 = Network.get(0);
        Node n1 = Network.get(1);

        CyclonSimple c1 = (CyclonSimple)n1.getProtocol(pid);
        c1.addNeighbor(n0);

        //CyclonSimple.subscribe(n0, n1);

        for (int i = 2; i < Network.size(); i++) {
            Node me = Network.get(i);
            int o = CDState.r.nextInt(i-1);
            CyclonSimple other = (CyclonSimple)Network.get(o).getProtocol(pid);
            other.addNeighbor(me);

            //ScampProtocol.subscribe(other, me);

        }

    }
}
