package test;

import example.cyclon.CyclonEntry;
import example.cyclon.CyclonSimple;
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

        Node me = createNode(555);
        List<CyclonEntry> cache = randomList2_10();
        List<CyclonEntry> sent = randomList2_5();
        List<CyclonEntry> received = randomList3_5();

        List<CyclonEntry> result = cyclon.merge(me,createNode(939), cache, received, sent);

        assertEquals("[1,5,6,12,91,13,86,87,88,9]", print(result));

    }

    @org.junit.Test
    public void testDiscard2() throws Exception {
        // BUG CONTEXT:
        //MyId:939
        // rec:{475|age:1}{718|age:1}{657|age:1}{37|age:1}{939|age:0}
        // sen:{232|age:1}{657|age:1}{483|age:1}{539|age:1}{895|age:1}
        //CACHE:[{264|age:1}, {718|age:1}, {840|age:1}, {37|age:1}, {475|age:1}, {659|age:1},
        //       {793|age:1}, {864|age:1}, {215|age:1}, {561|age:1}, {524|age:1}, {532|age:1},
        //       {292|age:1}, {967|age:1}]
        CyclonSimple cyclon = new CyclonSimple(20,5);
        List<CyclonEntry> cache = cache_20();
        List<CyclonEntry> rec = rec_5();
        List<CyclonEntry> sen = sen_5();
        Node self = createNode(939);

        List<CyclonEntry> result = cyclon.merge(self, createNode(939), cache, rec, sen);

        assertEquals("[264,718,840,37,475,659,793,864,215,561,524,532,292,967,657,232,483,539,895]", print(result));
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

    private static List<CyclonEntry> rec_5(){
        List<CyclonEntry> list = new ArrayList<CyclonEntry>();
        list.add(new CyclonEntry(1, createNode(475)));
        list.add(new CyclonEntry(1, createNode(718)));
        list.add(new CyclonEntry(1, createNode(657)));
        list.add(new CyclonEntry(1, createNode(37)));
        list.add(new CyclonEntry(0, createNode(939)));
        return list;
    }

    private static List<CyclonEntry> sen_5(){
        List<CyclonEntry> list = new ArrayList<CyclonEntry>();
        list.add(new CyclonEntry(1, createNode(232)));
        list.add(new CyclonEntry(1, createNode(657)));
        list.add(new CyclonEntry(1, createNode(483)));
        list.add(new CyclonEntry(1, createNode(539)));
        list.add(new CyclonEntry(1, createNode(895)));
        return list;
    }

    private static List<CyclonEntry> cache_20(){
        List<CyclonEntry> list = new ArrayList<CyclonEntry>();
        list.add(new CyclonEntry(1, createNode(264)));
        list.add(new CyclonEntry(1, createNode(718)));
        list.add(new CyclonEntry(1, createNode(840)));
        list.add(new CyclonEntry(1, createNode(37)));
        list.add(new CyclonEntry(1, createNode(475)));
        list.add(new CyclonEntry(1, createNode(659)));
        list.add(new CyclonEntry(1, createNode(793)));
        list.add(new CyclonEntry(1, createNode(864)));
        list.add(new CyclonEntry(1, createNode(215)));
        list.add(new CyclonEntry(1, createNode(561)));
        list.add(new CyclonEntry(1, createNode(524)));
        list.add(new CyclonEntry(1, createNode(532)));
        list.add(new CyclonEntry(1, createNode(292)));
        list.add(new CyclonEntry(1, createNode(967)));
        return list;
    }

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
        list.add(new CyclonEntry(0, createNode(555)));
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