package example;

import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 26/01/15.
 */
public interface PeerSamplingService {

    List<Node> getPeers();

    String debug();

    int callsInThisCycle();
    void clearCallsInCycle();

}
