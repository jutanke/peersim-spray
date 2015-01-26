package test;

import example.cyclon.PeerSamplingService;
import peersim.core.Node;
import peersim.core.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 26/01/15.
 */
public final class HelperForTest {
    private HelperForTest() {
    }


    /**
     * Dummy method!
     *
     * @param id
     * @return
     */
    public static final Node createNode(final long id) {
        return new Node() {
            @Override
            public Protocol getProtocol(int i) {
                return null;
            }

            @Override
            public int protocolSize() {
                return 1;
            }

            @Override
            public void setIndex(int index) {

            }

            @Override
            public int getIndex() {
                return 0;
            }

            @Override
            public long getID() {
                return id;
            }

            @Override
            public int getFailState() {
                return 0;
            }

            @Override
            public void setFailState(int failState) {

            }

            @Override
            public boolean isUp() {
                return false;
            }

            @Override
            public Object clone() {
                return this;
            }
        };
    }


    public static final PeerSamplingService createRPS(final long[] neighbors) {
        final List<Node> peers = new ArrayList<Node>();
        for (long e : neighbors) {
            peers.add(HelperForTest.createNode(e));
        }
        return new PeerSamplingService() {
            @Override
            public List<Node> getPeers() {
                return peers;
            }
        };
    }

}
