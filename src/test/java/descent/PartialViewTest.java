package descent;

import descent.spray.PartialView;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Created by julian on 04/05/15.
 */
public class PartialViewTest extends TestCase {

    /**
     * ctor
     * @param n
     */
    public PartialViewTest(String n) {
        super(n);
    }

    public static Test suite() {
        return new TestSuite(PartialViewTest.class);
    }

    @org.junit.Test
    public void testContains() {

        final PartialView pv = new PartialView();
        pv.add(Helper.createNode(1));
        pv.add(Helper.createNode(2));
        pv.add(Helper.createNode(3));

        assertFalse(pv.contains(Helper.createNode(4)));
        assertTrue(pv.contains(Helper.createNode(1)));
    }

}
