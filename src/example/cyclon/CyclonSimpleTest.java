package example.cyclon;

import example.webrtc.cyclon2.Cyclon;
import peersim.core.Node;
import peersim.core.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class CyclonSimpleTest {

    final int SIZE = 10;
    final int L = 5;

    @org.junit.Test
    public void testMerge() throws Exception {

    }

    @org.junit.Test
    public void testPopSmallest() throws Exception {
        CyclonSimple cyclon = new CyclonSimple(SIZE,L);
        List<CyclonEntry> list = randomList1();
        CyclonEntry smallest = cyclon.popSmallest(list);
        assertEquals(5, smallest.n.getID());
        assertEquals("[3,1,4,6,8,9]", print(list));
    }

    @org.junit.Test
    public void testDiscard() throws Exception {
        CyclonSimple cyclon = new CyclonSimple(SIZE,L);

        Node me = createNode(5);
        List<CyclonEntry> cache = randomList2_10();
        List<CyclonEntry> sent = randomList2_5();
        List<CyclonEntry> received = randomList3_5();

        List<CyclonEntry> result = cyclon.merge(me, cache, received, sent);

        assertEquals("[1,6,12,91,13,86,87,88,9,8]", print(result));

    }

    @org.junit.Test
    public void testDelete() throws Exception {

    }

    @org.junit.Test
    public void testIncreaseAge() throws Exception {

    }

    @org.junit.Test
    public void testSelectOldest() throws Exception {

    }

    // ==================================================
    // H E L P E R S
    // ==================================================

    private static List<CyclonEntry> randomList1(){
        List<CyclonEntry> list = new ArrayList<CyclonEntry>();
        list.add(new CyclonEntry(3, createNode(3)));
        list.add(new CyclonEntry(1, createNode(1)));
        list.add(new CyclonEntry(5, createNode(4)));
        list.add(new CyclonEntry(0, createNode(5)));
        list.add(new CyclonEntry(1, createNode(6)));
        list.add(new CyclonEntry(2, createNode(8)));
        list.add(new CyclonEntry(1, createNode(9)));
        return list;
    }

    private static List<CyclonEntry> randomList2_10(){
        List<CyclonEntry> list = new ArrayList<CyclonEntry>();
        list.add(new CyclonEntry(3, createNode(3)));
        list.add(new CyclonEntry(1, createNode(1)));
        list.add(new CyclonEntry(5, createNode(4)));
        list.add(new CyclonEntry(0, createNode(5)));
        list.add(new CyclonEntry(1, createNode(6)));
        list.add(new CyclonEntry(2, createNode(8)));
        list.add(new CyclonEntry(1, createNode(9)));
        list.add(new CyclonEntry(2, createNode(12)));
        list.add(new CyclonEntry(4, createNode(91)));
        list.add(new CyclonEntry(0, createNode(13)));
        return list;
    }

    private static List<CyclonEntry> randomList2_5(){
        List<CyclonEntry> list = new ArrayList<CyclonEntry>();
        list.add(new CyclonEntry(3, createNode(3)));
        list.add(new CyclonEntry(5, createNode(4)));
        list.add(new CyclonEntry(0, createNode(5)));
        list.add(new CyclonEntry(2, createNode(8)));
        list.add(new CyclonEntry(1, createNode(9)));
        return list;
    }

    private static List<CyclonEntry> randomList3_5(){
        List<CyclonEntry> list = new ArrayList<CyclonEntry>();
        list.add(new CyclonEntry(1, createNode(1)));
        list.add(new CyclonEntry(1, createNode(86)));
        list.add(new CyclonEntry(5, createNode(87)));
        list.add(new CyclonEntry(0, createNode(88)));
        list.add(new CyclonEntry(0, createNode(5)));
        return list;
    }


    private static String print(List<CyclonEntry> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (CyclonEntry e : list) {
            if (sb.length() > 1 ) sb.append(",");
            sb.append(e.n.getID());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Dummy method!
     * @param id
     * @return
     */
    private static Node createNode(final long id) {
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
            public Object clone(){
                return this;
            }
        };
    }
}