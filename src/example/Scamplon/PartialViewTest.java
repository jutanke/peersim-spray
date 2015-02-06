package example.Scamplon;

import org.junit.Test;
import peersim.core.Node;
import test.HelperForTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PartialViewTest {

    @Test
    public void testDegree() throws Exception {
        PartialView.TEST_ENV = true;
        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(b);
        view.add(c);
        assertEquals(2, view.degree());
    }

    @Test
    public void testIncrementAge() throws Exception {
        PartialView.TEST_ENV = true;
        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(b);
        view.add(c);
        view.incrementAge();
        assertEquals(" -> [{1|1|n}, {2|1|n}]", view.toString());
    }

    @Test
    public void testL() throws Exception {
        PartialView.TEST_ENV = true;
        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(b);
        view.add(c);
        assertEquals(1, view.l());
        Node d = HelperForTest.createNode(3);
        view.add(d);
        assertEquals(2, view.l());
        Node e = HelperForTest.createNode(4);
        view.add(e);
        assertEquals(2, view.l());
    }

    @Test
    public void testSubset() throws Exception {
        PartialView.TEST_ENV = true;
        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(b);
        view.add(c);
        Node d = HelperForTest.createNode(3);
        view.add(d);
        Node e = HelperForTest.createNode(4);
        view.add(e);
        List<PartialView.Entry> subset = view.subset();
        assertEquals(2, subset.size());
    }

    @Test
    public void testSubsetMinus1() throws Exception {
        PartialView.TEST_ENV = true;
        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(b);
        view.add(c);
        Node d = HelperForTest.createNode(3);
        view.add(d);
        Node e = HelperForTest.createNode(4);
        view.add(e);
        PartialView.Entry oldest = view.oldest();
        List<PartialView.Entry> subset = view.subsetMinus1(oldest);
        assertFalse(PartialView.contains(subset, oldest.node));
        assertEquals(1, subset.size());
    }

    @Test
    public void testOldest() {
        PartialView.TEST_ENV = true;
        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(a);
        view.add(b);
        view.add(c);
        List<PartialView.Entry> e = view.get(a);

        assertEquals(1, e.size());
        e.get(0).age = 5;

        PartialView.Entry oldest = view.oldest();
        assertEquals(0L, oldest.node.getID());

        e = view.get(a);
        assertTrue(e.get(0).isVolatile);
    }

    @Test
    public void testMerge() throws Exception {
        PartialView.TEST_ENV = true;
        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(b);
        view.add(c);
        Node d = HelperForTest.createNode(3);
        view.add(d);
        Node e = HelperForTest.createNode(4);
        view.add(e);
        PartialView.Entry oldest = view.oldest();


        List<PartialView.Entry> received = new ArrayList<PartialView.Entry>();
        received.add(HelperForTest.entry(7, 2));
        int otherViewSize = 2;

        view.subsetMinus1(oldest);
        view.merge(a, oldest.node, received, otherViewSize);

        assertFalse(view.contains(oldest.node));
        assertEquals(3, view.degree());
        assertEquals(" -> [{2|0|n}, {3|0|n}, {7|2|n}]", view.toString());
    }

    @Test
    public void testMerge1() throws Exception {
        PartialView.TEST_ENV = true;
        PartialView view = new PartialView();
        PartialView.Entry a = HelperForTest.entry(5, 1);
        a.isVolatile = true;
        PartialView.Entry b = HelperForTest.entry(2, 1);
        b.isVolatile = true;
        PartialView.Entry c = HelperForTest.entry(4, 1);
        view.out.add(a); view.out.add(b); view.out.add(c);

        int otherViewSize = 4;
        List<PartialView.Entry> received = new ArrayList<PartialView.Entry>();
        received.add(HelperForTest.entry(1,0));
        received.add(HelperForTest.entry(9,0));

        Node me = HelperForTest.createNode(1);
        PartialView.Entry oldest = view.oldest();

        assertEquals(5L, oldest.node.getID());

        view.merge(me, oldest.node, received, otherViewSize);

        assertEquals("[{4|1|n}, {9|0|n}, {2|1|n}]", view.out.toString());
    }

    @Test
    public void testSubset1() throws Exception {
        PartialView.TEST_ENV = true;

        Node a = HelperForTest.createNode(0);
        Node b = HelperForTest.createNode(1);
        Node c = HelperForTest.createNode(2);
        PartialView view = new PartialView();
        view.add(a); view.add(b); view.add(c);

        System.out.println("b ===> " +view);
        List<PartialView.Entry> subset = view.subset();
        System.out.println("a ===> " +view);

    }

    @Test
    public void testSubset2() throws Exception {
        PartialView.TEST_ENV = true;
    }

    @Test
    public void testContains() throws Exception {
        PartialView.TEST_ENV = true;
    }

    @Test
    public void testGet() throws Exception {
        PartialView.TEST_ENV = true;
    }

    @Test
    public void testRemove() throws Exception {
        PartialView.TEST_ENV = true;
    }

    @Test
    public void testRemoveVolatileResults() throws Exception {
        PartialView.TEST_ENV = true;
    }
}