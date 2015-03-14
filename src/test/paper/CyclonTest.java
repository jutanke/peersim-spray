package test.paper;

import example.paper.cyclon.Cyclon;
import example.paper.cyclon.CyclonEntry;
import test.HelperForTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by julian on 3/14/15.
 */
public class CyclonTest {

    final int SIZE = 10;
    final int L = 5;

    @org.junit.Test
    public void testInsert() throws Exception {
        Cyclon cyclon = new Cyclon(SIZE, L);
        List<CyclonEntry> ces = new ArrayList<CyclonEntry>();
        for (int i = 0; i < 20; i++) {
            ces.add(new CyclonEntry(0, HelperForTest.createNode(i)));
        }
        ces.get(3).age = 10;
        ces.get(2).age = 7;
        ces.get(5).age = 8;

        for (CyclonEntry ce : ces) {
            cyclon.insert(ce);
        }
        assertEquals(10, cyclon.degree());
        assertEquals(3L, cyclon.popOldest().getID());
        assertEquals(5L, cyclon.popOldest().getID());
        assertEquals(2L, cyclon.popOldest().getID());
    }

    @org.junit.Test
    public void mergeConflict() throws Exception {
        Cyclon cyclon = new Cyclon(SIZE, L);
        List<CyclonEntry> sent = new ArrayList<CyclonEntry>();
        List<CyclonEntry> received = new ArrayList<CyclonEntry>();
        long ownID = 0;

        cyclon.insert(new CyclonEntry(3, HelperForTest.createNode(3)));
        cyclon.insert(new CyclonEntry(7, HelperForTest.createNode(1)));
        cyclon.insert(new CyclonEntry(1, HelperForTest.createNode(2)));
        cyclon.insert(new CyclonEntry(6, HelperForTest.createNode(4)));
        cyclon.insert(new CyclonEntry(0, HelperForTest.createNode(5)));

        sent.add(new CyclonEntry(12, HelperForTest.createNode(6)));
        sent.add(new CyclonEntry(13, HelperForTest.createNode(7)));
        sent.add(new CyclonEntry(14, HelperForTest.createNode(8)));
        sent.add(new CyclonEntry(15, HelperForTest.createNode(9)));
        sent.add(new CyclonEntry(0, HelperForTest.createNode(0)));

        received.add(new CyclonEntry(8, HelperForTest.createNode(11)));
        received.add(new CyclonEntry(4, HelperForTest.createNode(12)));
        received.add(new CyclonEntry(2, HelperForTest.createNode(4)));
        received.add(new CyclonEntry(6, HelperForTest.createNode(5)));
        received.add(new CyclonEntry(0, HelperForTest.createNode(0)));

        cyclon.insertReceivedItems(received, sent, ownID);

        assertEquals("5,2,3,12,4,1,11,6,7,8", cyclon.debugIds());
    }

    @org.junit.Test
    public void merge() throws Exception {
        Cyclon cyclon = new Cyclon(SIZE, L);
        List<CyclonEntry> sent = new ArrayList<CyclonEntry>();
        List<CyclonEntry> received = new ArrayList<CyclonEntry>();
        long ownID = 0;

        cyclon.insert(new CyclonEntry(3, HelperForTest.createNode(3)));
        cyclon.insert(new CyclonEntry(7, HelperForTest.createNode(1)));
        cyclon.insert(new CyclonEntry(1, HelperForTest.createNode(2)));
        cyclon.insert(new CyclonEntry(6, HelperForTest.createNode(4)));
        cyclon.insert(new CyclonEntry(0, HelperForTest.createNode(5)));

        sent.add(new CyclonEntry(12, HelperForTest.createNode(6)));
        sent.add(new CyclonEntry(13, HelperForTest.createNode(7)));
        sent.add(new CyclonEntry(14, HelperForTest.createNode(8)));
        sent.add(new CyclonEntry(15, HelperForTest.createNode(9)));
        sent.add(new CyclonEntry(0, HelperForTest.createNode(0)));

        received.add(new CyclonEntry(8, HelperForTest.createNode(11)));
        received.add(new CyclonEntry(9, HelperForTest.createNode(12)));
        received.add(new CyclonEntry(10, HelperForTest.createNode(13)));
        received.add(new CyclonEntry(11, HelperForTest.createNode(14)));
        received.add(new CyclonEntry(12, HelperForTest.createNode(15)));

        cyclon.insertReceivedItems(received, sent, ownID);

        assertEquals("5,2,3,4,1,11,12,13,14,15", cyclon.debugIds());
    }

    @org.junit.Test
    public void popsubset() throws Exception {
        Cyclon cyclon = new Cyclon(SIZE, L);
        cyclon.insert(new CyclonEntry(3, HelperForTest.createNode(3)));
        cyclon.insert(new CyclonEntry(7, HelperForTest.createNode(1)));
        cyclon.insert(new CyclonEntry(1, HelperForTest.createNode(2)));
        cyclon.insert(new CyclonEntry(6, HelperForTest.createNode(4)));
        cyclon.insert(new CyclonEntry(0, HelperForTest.createNode(5)));
        cyclon.insert(new CyclonEntry(12, HelperForTest.createNode(6)));
        cyclon.insert(new CyclonEntry(13, HelperForTest.createNode(7)));
        cyclon.insert(new CyclonEntry(14, HelperForTest.createNode(8)));
        cyclon.insert(new CyclonEntry(15, HelperForTest.createNode(9)));
        cyclon.insert(new CyclonEntry(0, HelperForTest.createNode(10)));

        assertEquals(10, cyclon.degree());

        List<CyclonEntry> pop = cyclon.popRandomSubset(5);
        assertEquals(5, pop.size());
        assertEquals(5, cyclon.degree());

        cyclon.insertReceivedItems(pop, new ArrayList<CyclonEntry>(), 0L);
        assertEquals(10, cyclon.degree());
    }

}
