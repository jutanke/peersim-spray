package test.paper;

import example.paper.cyclon.CyclonEntry;
import example.paper.cyclon.CyclonProtocol;
import peersim.core.Node;
import test.HelperForTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by julian on 3/24/15.
 */
public class SimpleCyclonTest {

    Node n0 = HelperForTest.createNode(0);
    Node n1 = HelperForTest.createNode(1);
    Node n2 = HelperForTest.createNode(2);
    Node n3 = HelperForTest.createNode(3);
    Node n4 = HelperForTest.createNode(4);
    Node n5 = HelperForTest.createNode(5);
    Node n6 = HelperForTest.createNode(6);
    Node n7 = HelperForTest.createNode(7);
    Node n8 = HelperForTest.createNode(8);
    Node n9 = HelperForTest.createNode(9);
    Node n10 = HelperForTest.createNode(10);
    Node n11 = HelperForTest.createNode(11);
    Node n12 = HelperForTest.createNode(12);

    @org.junit.Test
    public void mergeSimple() throws Exception {
        CyclonProtocol.size = 7;
        // l = 3
        Node me = HelperForTest.createNode(50);
        Node destination = n0;
        List<CyclonEntry> partialView = new ArrayList<CyclonEntry>();
        partialView.add(new CyclonEntry(12, n0));
        partialView.add(new CyclonEntry(11, n1));
        partialView.add(new CyclonEntry(10, n2));
        partialView.add(new CyclonEntry(9, n3));
        partialView.add(new CyclonEntry(8, n4));
        partialView.add(new CyclonEntry(7, n5));
        partialView.add(new CyclonEntry(0, n6));

        List<CyclonEntry> received = new ArrayList<CyclonEntry>();
        received.add(new CyclonEntry(24, n7));
        received.add(new CyclonEntry(20, n8));
        received.add(new CyclonEntry(21, n9));

        List<CyclonEntry> sent = new ArrayList<CyclonEntry>();
        sent.add(new CyclonEntry(0, me));
        sent.add(new CyclonEntry(11, n1));
        sent.add(new CyclonEntry(7, n5));

        partialView = CyclonProtocol.insertIntoPartialView(
                me,
                destination,
                partialView,
                received,
                sent);
        assertEquals("6,4,3,2,8,9,7", list(partialView));
    }


    @org.junit.Test
    public void mergeOneConflict() throws Exception {
        CyclonProtocol.size = 7;
        // l = 3
        Node me = HelperForTest.createNode(50);
        Node destination = n0;
        List<CyclonEntry> partialView = new ArrayList<CyclonEntry>();
        partialView.add(new CyclonEntry(12, n0));
        partialView.add(new CyclonEntry(11, n1));
        partialView.add(new CyclonEntry(10, n2));
        partialView.add(new CyclonEntry(9, n3));
        partialView.add(new CyclonEntry(8, n4));
        partialView.add(new CyclonEntry(7, n5));
        partialView.add(new CyclonEntry(0, n6));

        List<CyclonEntry> received = new ArrayList<CyclonEntry>();
        received.add(new CyclonEntry(24, n6));
        received.add(new CyclonEntry(20, n8));
        received.add(new CyclonEntry(21, n9));

        List<CyclonEntry> sent = new ArrayList<CyclonEntry>();
        sent.add(new CyclonEntry(0, me));
        sent.add(new CyclonEntry(11, n1));
        sent.add(new CyclonEntry(7, n5));

        partialView = CyclonProtocol.insertIntoPartialView(
                me,
                destination,
                partialView,
                received,
                sent);
        assertEquals("6,5,4,3,2,8,9", list(partialView));
    }

    @org.junit.Test
    public void mergeThreeConflict() throws Exception {
        CyclonProtocol.size = 7;
        // l = 3
        Node me = HelperForTest.createNode(50);
        Node destination = n0;
        List<CyclonEntry> partialView = new ArrayList<CyclonEntry>();
        partialView.add(new CyclonEntry(12, n0));
        partialView.add(new CyclonEntry(11, n1));
        partialView.add(new CyclonEntry(10, n2));
        partialView.add(new CyclonEntry(9, n3));
        partialView.add(new CyclonEntry(8, n4));
        partialView.add(new CyclonEntry(7, n5));
        partialView.add(new CyclonEntry(0, n6));

        List<CyclonEntry> received = new ArrayList<CyclonEntry>();
        received.add(new CyclonEntry(24, n6));
        received.add(new CyclonEntry(21, n4));
        received.add(new CyclonEntry(20, n1));

        List<CyclonEntry> sent = new ArrayList<CyclonEntry>();
        sent.add(new CyclonEntry(0, me));
        sent.add(new CyclonEntry(11, n1));
        sent.add(new CyclonEntry(7, n5));

        partialView = CyclonProtocol.insertIntoPartialView(
                me,
                destination,
                partialView,
                received,
                sent);
        assertEquals(6, partialView.size());
        assertEquals("6,5,4,3,2,1", list(partialView));
    }

    @org.junit.Test
    public void mergeBug1() throws Exception {
        CyclonProtocol.size = 7;
        // l = 3
        Node me = HelperForTest.createNode(50);
        Node destination = n0;
        List<CyclonEntry> partialView = new ArrayList<CyclonEntry>();
        partialView.add(new CyclonEntry(12, n0));
        partialView.add(new CyclonEntry(11, n1));
        partialView.add(new CyclonEntry(10, n2));
        partialView.add(new CyclonEntry(9, n3));
        partialView.add(new CyclonEntry(8, n4));
        partialView.add(new CyclonEntry(7, n5));
        partialView.add(new CyclonEntry(0, n6));

        List<CyclonEntry> received = new ArrayList<CyclonEntry>();
        received.add(new CyclonEntry(24, n6));
        received.add(new CyclonEntry(21, n4));
        received.add(new CyclonEntry(20, n1));

        List<CyclonEntry> sent = new ArrayList<CyclonEntry>();
        sent.add(new CyclonEntry(0, me));
        sent.add(new CyclonEntry(11, n1));
        sent.add(new CyclonEntry(7, n5));

        partialView = CyclonProtocol.insertIntoPartialView(
                me,
                destination,
                partialView,
                received,
                sent);
        assertEquals(6, partialView.size());
        assertEquals("6,5,4,3,2,1", list(partialView));
    }

    /**
     * make comparable
     * @param list
     * @return
     */
    private static String list(final List<CyclonEntry> list) {
        StringBuilder sb = new StringBuilder();
        Collections.sort(list);
        Collections.reverse(list);
        //for (CyclonEntry ce : list) {
        for (int i = 0; i < list.size();i++) {
            final CyclonEntry ce = list.get(i);
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ce.n.getID());
        }
        return sb.toString();
    }

}
