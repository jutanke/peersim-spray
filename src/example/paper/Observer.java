package example.paper;

import example.PeerSamplingService;
import example.webrtc.data.DictGraph;
import peersim.config.Configuration;
import peersim.config.MissingParameterException;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

/**
 * Created by julian on 3/15/15.
 */
public class Observer implements Control {

    private static final String PROTOCOL = "lnk";
    private static final String PROTOCOL_0 = "0";

    // =============================================
    // C T O R
    // =============================================

    private int pid;

    public Observer(String name) {

        try {
            this.pid = Configuration.lookupPid(PROTOCOL);
        } catch (MissingParameterException e) {
            this.pid = Configuration.lookupPid(PROTOCOL_0);
        }

    }

    // =============================================
    // E X E C
    // =============================================

    @Override
    public boolean execute() {

        final int STEP = 10;
        final DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();

        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < Network.size(); i++) {
            GeneralNode n = (GeneralNode) Network.get(i);
            PeerSamplingService pss = (PeerSamplingService) n.getProtocol(pid);
            observer.add(n, pss);
            final int size = pss.getPeers().size();
            if (size < min) {
                min = size;
            }
            if (size > max) {
                max = size;
            }
        }
        System.err.println("MIN:" + min + ", MAX:" + max);

        if (CommonState.getTime() % 500 == 0) {
            System.out.println(observer.avgReachablePaths(0).reachQuota);
        }



        return false;
    }

    private long randomId() {
        return Network.get(CommonState.r.nextInt(Network.size())).getID();
    }
}
