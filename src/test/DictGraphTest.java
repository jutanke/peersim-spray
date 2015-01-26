package test;

import example.cyclon.PeerSamplingService;
import example.webrtc.data.DictGraph;
import org.junit.Test;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DictGraphTest {

    @Test
    public void testAdd() throws Exception {

        DictGraph g = DictGraph.getSingleton(10);

        Peer a = new Peer(0, new long[]{1,2});
        Peer b = new Peer(1, new long[]{0,2});
        Peer c = new Peer(2, new long[]{1,0});

        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);

        DictGraph.AvgReachablePaths avg = g.avgReachablePaths(0);

        System.out.println(avg);

    }

    @Test
    public void testAvgReachablePaths() throws Exception {

    }

    @Test
    public void testLocalClusterCoefficient() throws Exception {

    }

    @Test
    public void testNeighbourhood() throws Exception {

    }

    @Test
    public void testDijkstra() throws Exception {

    }

    /* ====================================================
     * H E L P E R S
     * ====================================================*/


//    private PeerSamplingService
//
    private class Peer {

        public final Node node;
        public final PeerSamplingService rps;

        public Peer(long me, long[] neighbors) {
            this.node = HelperForTest.createNode(me);
            this.rps = HelperForTest.createRPS(neighbors);
        }
    }


 }