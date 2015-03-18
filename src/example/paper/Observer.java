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

        final DictGraph observer = DictGraph.getSingleton(Network.size());
        observer.reset();

        for (int i = 0; i < Network.size(); i++) {
            GeneralNode n = (GeneralNode) Network.get(i);
            PeerSamplingService pss = (PeerSamplingService) n.getProtocol(pid);
            //System.out.println(n);
            observer.add(n, pss);


            System.err.println(n.getID() + ":" + pss);
        }

        if (false) {
            double a = observer.avgReachablePaths(randomId()).avg;
            double b = observer.avgReachablePaths(randomId()).avg;
            double c = observer.avgReachablePaths(randomId()).avg;
            double d = observer.avgReachablePaths(randomId()).avg;
            double e = observer.avgReachablePaths(randomId()).avg;
            double f = observer.avgReachablePaths(randomId()).avg;
            double g = observer.avgReachablePaths(randomId()).avg;
            double h = observer.avgReachablePaths(randomId()).avg;
            double i = observer.avgReachablePaths(randomId()).avg;
            double j = observer.avgReachablePaths(randomId()).avg;

            double avg = (a + b + c + d + e + f + g + h + i + j) / 10;
            System.out.println(avg);
        } else if (false) {

            if (CommonState.getTime() % 50 == 0) {
                System.out.println(observer.averagePathLength());
            }

        }

        if (CommonState.getTime() == 19) {

            System.out.println(observer.toGraph());
        }

        return false;
    }

    private long randomId() {
        return Network.get(CommonState.r.nextInt(Network.size())).getID();
    }
}
