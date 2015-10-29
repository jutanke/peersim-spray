package descent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import descent.spray.Spray;
import descent.spray.SprayMessage;

public class SprayTest extends TestCase {

	public SprayTest() {
		// TODO Auto-generated constructor stub
	}

	public static Test suite() {
		return new TestSuite(SprayTest.class);
	}

	@org.junit.Test
	public void testFirstContactMustMerge() {
		Spray s1 = new Spray();
		Spray s2 = new Spray();

		s1.to.add(0);
		s2.to.add(1);

		SprayMessage m = new SprayMessage(null, s1.from, -1, s1.to);

		s2.isFutureMerge(m);
		assertTrue(s2.mustMerge);
		assertTrue(s2.isMerge(m));
	}

	public void testFirstResponseMustMerge() {
		Spray s1 = new Spray();
		Spray s2 = new Spray();

		s1.to.add(0);
		s2.to.add(1);

		SprayMessage m = new SprayMessage(null, s1.from, -1, s1.to);
		s2.isFutureMerge(m);

		SprayMessage r = new SprayMessage(null, s2.from, 2, s2.to);
		s1.isFutureMerge(r);
		assertTrue(s1.mustMerge);
		assertTrue(s1.isMerge(r));
	}

	public void testSameNetworkNotMergedWithMerged() {
		Spray s1 = new Spray();
		s1.from.add(0);
		s1.to.add(0);
		s1.to.add(1);

		Spray s2 = new Spray();
		s2.to.add(0);

		SprayMessage m = new SprayMessage(null, s2.from, -1, s2.to);
		s1.isFutureMerge(m);
		assertFalse(s1.mustMerge);
		assertFalse(s1.isMerge(m));

		SprayMessage r = new SprayMessage(null, s1.from, 2, s1.to);
		s2.isFutureMerge(r);
		assertTrue(s2.mustMerge);
		assertFalse(s2.isMerge(r));
	}
}
