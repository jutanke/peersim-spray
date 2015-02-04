package example.scampXcyclon;

import org.junit.Test;
import peersim.core.Node;
import test.HelperForTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ScamplonViewTest {

    @Test
    public void testOldContains() throws Exception {
        Node two = HelperForTest.createNode(2);
        ScamplonView view = new ScamplonView();
        view.addToOut(HelperForTest.createNode(0));
        view.addToOut(HelperForTest.createNode(1));
        view.addToOut(two);
        assertTrue(view.outContains(two));
    }

    @Test
    public void testInContains() throws Exception {
        Node two = HelperForTest.createNode(2);
        ScamplonView view = new ScamplonView();
        view.addToIn(HelperForTest.createNode(0));
        view.addToIn(HelperForTest.createNode(1));
        view.addToIn(two);
        assertTrue(view.inContains(two));
    }

    @Test
    public void testL() throws Exception {
        Node two = HelperForTest.createNode(2);
        ScamplonView view = new ScamplonView();
        view.addToOut(HelperForTest.createNode(0));
        view.addToOut(HelperForTest.createNode(1));
        view.addToOut(two);
        assertEquals(2, view.l());
    }

    @Test
    public void testC() throws Exception {
        Node two = HelperForTest.createNode(2);
        ScamplonView view = new ScamplonView();
        view.addToOut(HelperForTest.createNode(0));
        view.addToOut(HelperForTest.createNode(1));
        view.addToOut(two);
        assertEquals(3, view.c());
    }

    @Test
    public void testOldest() throws Exception {
        Node two = HelperForTest.createNode(2);
        ScamplonView view = new ScamplonView();
        view.addToOut(HelperForTest.createNode(0));
        view.incrementAge();
        view.addToOut(HelperForTest.createNode(1));
        view.addToOut(two);
        assertEquals(0, view.oldest().node.getIndex());
    }

    @Test
    public void testSubsetMinus1() throws Exception {
        /*
        Node two = HelperForTest.createNode(2);
        ScamplonView view = new ScamplonView();
        view.addToOut(HelperForTest.createNode(0));
        view.incrementAge();
        view.addToOut(HelperForTest.createNode(1));
        view.addToOut(two);
        System.out.println(view.subsetMinus1(two));
        assertEquals(1, view.subsetMinus1(two));
        */
    }

    @Test
    public void testSubset() throws Exception {

    }

    @Test
    public void testIncrementAge() throws Exception {
        Node two = HelperForTest.createNode(2);
        ScamplonView view = new ScamplonView();
        view.addToOut(HelperForTest.createNode(0));
        view.addToOut(HelperForTest.createNode(1));
        view.addToOut(two);

        view.incrementAge();
        assertEquals(1, view.out.get(0).age);
        assertEquals(1, view.out.get(1).age);
        assertEquals(1, view.out.get(2).age);
    }

    @Test
    public void testMerge() throws Exception {
        ScamplonView view = new ScamplonView();

        Node me = HelperForTest.createNode(0);
        PartialViewEntry t = HelperForTest.createPartialViewEntry(5, 9);
        List<PartialViewEntry> sent = new ArrayList<PartialViewEntry>();
        sent.add(HelperForTest.createPartialViewEntry(1, 3));
        sent.add(HelperForTest.createPartialViewEntry(2, 4));
        sent.add(HelperForTest.createPartialViewEntry(0, 0));
        List<PartialViewEntry> received = new ArrayList<PartialViewEntry>();
        received.add(HelperForTest.createPartialViewEntry(1, 5));
        received.add(HelperForTest.createPartialViewEntry(4, 1));
        received.add(HelperForTest.createPartialViewEntry(5, 0));

        view.out.add(HelperForTest.createPartialViewEntry(1, 3));
        view.out.add(HelperForTest.createPartialViewEntry(2, 4));
        view.out.add(t);
        view.out.add(HelperForTest.createPartialViewEntry(6, 3));
        view.out.add(HelperForTest.createPartialViewEntry(7, 3));
        view.out.add(HelperForTest.createPartialViewEntry(8, 3));


        //List<PartialViewEntry> result = view.cut(sent, received);
        view.merge(me, t, sent, received, 0);

        System.out.println(view);

    }

    @Test
    public void testMerge1() throws Exception {
        ScamplonView view = new ScamplonView();
        view.out.add(HelperForTest.createPartialViewEntry(3, 1));
        view.out.add(HelperForTest.createPartialViewEntry(5, 1));
        view.out.add(HelperForTest.createPartialViewEntry(1, 1));

        Node me = HelperForTest.createNode(2);

        PartialViewEntry oldest = HelperForTest.createPartialViewEntry(1, 1);

        List<PartialViewEntry> sent = new ArrayList<PartialViewEntry>();
        sent.add(HelperForTest.createPartialViewEntry(3, 1));
        sent.add(HelperForTest.createPartialViewEntry(2, 0));

        List<PartialViewEntry> received = new ArrayList<PartialViewEntry>();
        received.add(HelperForTest.createPartialViewEntry(5, 0));

        view.merge(me, oldest, sent, received, 0);

        assertEquals(3, view.out.size());
        assertEquals("[{3|1}, {1|1}, {5|0}]", view.out.toString());

    }

    @Test
    public void testMerge2() throws Exception {
        ScamplonView view = new ScamplonView();
        view.out.add(HelperForTest.createPartialViewEntry(8, 1));
        view.out.add(HelperForTest.createPartialViewEntry(9, 1));
        view.out.add(HelperForTest.createPartialViewEntry(5, 2));

        Node me = HelperForTest.createNode(4);

        PartialViewEntry oldest = null;

        List<PartialViewEntry> sent = new ArrayList<PartialViewEntry>();
        sent.add(HelperForTest.createPartialViewEntry(5, 2));

        List<PartialViewEntry> received = new ArrayList<PartialViewEntry>();
        received.add(HelperForTest.createPartialViewEntry(6, 0));

        view.merge(me, oldest, sent, received, -1);


        assertEquals(2, view.out.size());
        assertEquals("[{3|1}, {1|1}, {5|0}]", view.out.toString());

    }

    @Test
    public void testRemoveN() throws Exception {

    }

    @Test
    public void testCut() throws Exception {
        List<PartialViewEntry> a = new ArrayList<PartialViewEntry>();
        a.add(HelperForTest.createPartialViewEntry(0, 2));
        a.add(HelperForTest.createPartialViewEntry(2, 2));
        a.add(HelperForTest.createPartialViewEntry(1, 2));
        a.add(HelperForTest.createPartialViewEntry(5, 2));
        List<PartialViewEntry> b = new ArrayList<PartialViewEntry>();
        b.add(HelperForTest.createPartialViewEntry(0, 2));
        b.add(HelperForTest.createPartialViewEntry(2, 2));
        b.add(HelperForTest.createPartialViewEntry(1, 2));
        b.add(HelperForTest.createPartialViewEntry(3, 2));
        ScamplonView view = new ScamplonView();
        List<PartialViewEntry> result = view.cut(a, b);
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).node.getID());
    }

    @Test
    public void testFilter() throws Exception {
        ScamplonView view = new ScamplonView();
        List<Node> filters = new ArrayList<Node>();
        filters.add(HelperForTest.createNode(0));
        filters.add(HelperForTest.createNode(2));
        filters.add(HelperForTest.createNode(1));
        List<PartialViewEntry> list = new ArrayList<PartialViewEntry>();
        list.add(HelperForTest.createPartialViewEntry(0, 2));
        list.add(HelperForTest.createPartialViewEntry(2, 2));
        list.add(HelperForTest.createPartialViewEntry(1, 2));
        list.add(HelperForTest.createPartialViewEntry(5, 2));
        List<PartialViewEntry> result = view.filter(list, filters);
        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).node.getID());
    }

    @Test
    public void testFilter1() throws Exception {

    }
}