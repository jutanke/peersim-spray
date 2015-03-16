package example.paper;

import example.PeerSamplingService;
import example.webrtc.data.DictGraph;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

/**
 * Created by julian on 3/15/15.
 */
public class Observer implements Control {

    private static final String PROTOCOL = "lnk";

    // =============================================
    // C T O R
    // =============================================

    private int pid;

    public Observer(String name) {
        this.pid = Configuration.lookupPid(PROTOCOL);
    }

    // =============================================
    // E X E C
    // =============================================

    @Override
    public boolean execute() {

        final DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();

        for (int i = 0; i < Network.size(); i++) {
            GeneralNode n = (GeneralNode) Network.get(i);
            PeerSamplingService pss = (PeerSamplingService) n.getProtocol(pid);
            //System.out.println(n);
            observer.add(n, pss);
        }

        System.out.println(observer.meanClusterCoefficient());

        return false;
    }
}
