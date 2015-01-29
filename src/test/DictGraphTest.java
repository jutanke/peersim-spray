package test;

import example.PeerSamplingService;
import example.webrtc.data.DictGraph;
import org.junit.Test;
import peersim.core.Node;

import java.util.Map;

import static org.junit.Assert.*;

public class DictGraphTest {

    private static double DELTA_FLOAT = 0.01;

    @Test
    public void testAdd() throws Exception {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        DictGraph.AvgReachablePaths avg = g.avgReachablePaths(0);
        assertEquals(1.0, avg.avg, DELTA_FLOAT);
        assertEquals(1.0, avg.reachQuota, DELTA_FLOAT);
        assertEquals(3, avg.count);
    }

    @Test
    public void averagePathLength() {
        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);

        double avg = g.averagePathLength();

        System.out.println("AVG: " + avg);
    }

    @Test
    public void testAvgReachablePaths() throws Exception {
        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});
        Peer d = new Peer(3, new long[0]);
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        DictGraph.AvgReachablePaths avg = g.avgReachablePaths(0);
        assertEquals(3, avg.count);
        assertEquals(0.75, avg.reachQuota, DELTA_FLOAT);
        assertEquals(1.0, avg.avg, DELTA_FLOAT);
        assertEquals(4, avg.total);
    }

    @Test
    public void testAvgReachablePaths2() throws Exception {
        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});
        Peer d = new Peer(3, new long[0]);
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        DictGraph.AvgReachablePaths avg = g.avgReachablePaths(3);
        assertEquals(1, avg.count);
        assertEquals(0.25, avg.reachQuota, DELTA_FLOAT);
        assertEquals(0.0, avg.avg, DELTA_FLOAT);
        assertEquals(4, avg.total);
    }

    @Test
    public void testAvgReachablePaths3() throws Exception {
        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0, 3});
        Peer d = new Peer(3, new long[]{2, 4});
        Peer e = new Peer(4, new long[]{3, 5});
        Peer f = new Peer(5, new long[]{4, 6});
        Peer gq = new Peer(6, new long[]{5, 7});
        Peer h = new Peer(7, new long[]{6});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        g.add(e.node, e.rps);
        g.add(f.node, f.rps);
        g.add(h.node, h.rps);
        g.add(gq.node, gq.rps);
        DictGraph.AvgReachablePaths avg = g.avgReachablePaths(3);
        System.out.println(avg);
        assertEquals(8, avg.count);
        assertEquals(1, avg.reachQuota, DELTA_FLOAT);
        assertEquals(2.142857142857143, avg.avg, DELTA_FLOAT);
        assertEquals(8, avg.total);
    }

    @Test
    public void testLocalClusterCoefficient() throws Exception {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0, 3});
        Peer d = new Peer(3, new long[]{2, 4});
        Peer e = new Peer(4, new long[]{3, 5});
        Peer f = new Peer(5, new long[]{4, 6, 0});
        Peer gq = new Peer(6, new long[]{5, 7});
        Peer h = new Peer(7, new long[]{6});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        g.add(e.node, e.rps);
        g.add(f.node, f.rps);
        g.add(h.node, h.rps);
        g.add(gq.node, gq.rps);
        double cl = g.localClusterCoefficient(0);
        assertEquals(1.0/3, cl, DELTA_FLOAT);
    }

    @Test
    public void testLocalClusterCoefficient2() throws Exception {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer b = new Peer(1, new long[]{2, 5});
        Peer c = new Peer(2, new long[]{5, 3});
        Peer d = new Peer(3, new long[]{4});
        Peer e = new Peer(4, new long[]{5,6,3});
        Peer f = new Peer(5, new long[]{4,1});
        Peer h = new Peer(6, new long[]{});
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        g.add(e.node, e.rps);
        g.add(f.node, f.rps);
        g.add(h.node, h.rps);
        assertEquals(1.0, g.localClusterCoefficient(1), DELTA_FLOAT);
        assertEquals(1.0/3, g.localClusterCoefficient(2), DELTA_FLOAT);
        assertEquals(0, g.localClusterCoefficient(3), DELTA_FLOAT);
        assertEquals(0, g.localClusterCoefficient(4), DELTA_FLOAT);
        assertEquals(1.0/3, g.localClusterCoefficient(5), DELTA_FLOAT);
        assertEquals(0, g.localClusterCoefficient(6), DELTA_FLOAT);

        //System.out.println("1/3: " + g.meanClusterCoefficient());

    }

    @Test
    public void testMeanPathLength() throws Exception {
        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        DictGraph.MeanPathLength mean = g.meanPathLength();
        assertEquals(1.0, mean.avg, DELTA_FLOAT);
        assertEquals(1.0, mean.maxReachQuota, DELTA_FLOAT);
        assertEquals(1.0, mean.minReachQuota, DELTA_FLOAT);
        assertEquals(1.0, mean.reachQuota, DELTA_FLOAT);
        Peer d = new Peer(3, new long[0]);
        g.add(d.node, d.rps);
        mean = g.meanPathLength();
        assertEquals(0.75, mean.avg, DELTA_FLOAT);
        assertEquals(0.625, mean.reachQuota, DELTA_FLOAT);
        assertEquals(0.25, mean.minReachQuota, DELTA_FLOAT);
        assertEquals(0.75, mean.maxReachQuota, DELTA_FLOAT);
    }

    @Test
    public void testNeighbourhood() throws Exception {

    }

    @Test
    public void dijkstraUndirected() throws Exception {
        DictGraph g = DictGraph.getSingleton(10);
        g.reset();

        Peer a = new Peer(0, new long[]{1});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});

        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);

        Map<Long, Integer> dist = g.dijkstraUndirected(0);

        System.out.println(dist);
        assertEquals("{0=0, 1=1, 2=1}", dist.toString());
    }

    @Test
    public void dijkstraUndirected2() throws Exception {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();

        Peer a = new Peer(0, new long[]{1});
        Peer b = new Peer(1, new long[]{0});
        Peer c = new Peer(2, new long[]{1, 0});

        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);

        Peer d = new Peer(3, new long[0]);
        g.add(d.node, d.rps);

        Map<Long, Integer> dist = g.dijkstraUndirected(0);
        assertEquals("{0=0, 1=1, 2=1, 3=-1}", dist.toString());
    }

    @Test
    public void testDijkstra() throws Exception {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();

        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});

        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);

        Map<Long, Integer> dist = g.dijkstra(0);
        assertEquals("{0=0, 1=1, 2=1}", dist.toString());

        Peer d = new Peer(3, new long[0]);
        g.add(d.node, d.rps);

        dist = g.dijkstra(0);
        assertEquals("{0=0, 1=1, 2=1, 3=-1}", dist.toString());
    }

    @Test
    public void testDijkstra2() throws Exception {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();

        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0, 3});
        Peer d = new Peer(3, new long[]{2, 4});
        Peer e = new Peer(4, new long[]{3});

        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        g.add(e.node, e.rps);

        Map<Long, Integer> dist = g.dijkstra(0);
        assertEquals("{0=0, 1=1, 2=1, 3=2, 4=3}", dist.toString());
    }

    @Test
    public void testDijkstra3() {
        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0});
        Peer d = new Peer(3, new long[0]);
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);

        Map<Long, Integer> dist = g.dijkstra(3);
        assertEquals("{0=-1, 1=-1, 2=-1, 3=0}", dist.toString());
    }

    @Test
    public void testInDegree() {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0, 3});
        Peer d = new Peer(3, new long[]{2, 1});
        Peer f = new Peer(4, new long[]{3, 1, 0});
        Peer e = new Peer(5, new long[]{3, 1, 4});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        g.add(f.node, f.rps);
        g.add(e.node, e.rps);

        assertEquals("[1,1,0,3,0,1]", HelperForTest.print(g.inDegreeAsHistogram()));

        g.reset();
        a = new Peer(0, new long[]{1, 2});
        b = new Peer(1, new long[]{0, 2});
        c = new Peer(2, new long[]{1, 0});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);

        assertEquals("[0,0,3]", HelperForTest.print(g.inDegreeAsHistogram()));

        g.reset();
        a = new Peer(0, new long[]{1, 2});
        b = new Peer(1, new long[]{0, 2});
        c = new Peer(2, new long[]{1, 0});
        d = new Peer(3, new long[]{1});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);

        assertEquals("[1,0,2,1]", HelperForTest.print(g.inDegreeAsHistogram()));
    }

    @Test
    public void testInDegrees() {

        DictGraph g = DictGraph.getSingleton(10);
        g.reset();
        Peer a = new Peer(0, new long[]{1, 2});
        Peer b = new Peer(1, new long[]{0, 2});
        Peer c = new Peer(2, new long[]{1, 0, 3});
        Peer d = new Peer(3, new long[]{2, 1});
        Peer f = new Peer(4, new long[]{3, 1, 0});
        Peer e = new Peer(5, new long[]{3, 1, 4});
        g.add(a.node, a.rps);
        g.add(b.node, b.rps);
        g.add(c.node, c.rps);
        g.add(d.node, d.rps);
        g.add(f.node, f.rps);
        g.add(e.node, e.rps);

        assertEquals("[3,5,3,3,1,0]", HelperForTest.print(g.inDegrees()));
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