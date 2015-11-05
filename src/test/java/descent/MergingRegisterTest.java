package descent;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import descent.spray.MergingRegister;
import descent.spray.SprayMessage;

public class MergingRegisterTest extends TestCase {

	public MergingRegisterTest() {
		// TODO Auto-generated constructor stub
	}

	public static Test suite() {
		return new TestSuite(MergingRegisterTest.class);
	}

	// [{0}] -> [{1}]
	public void testFirstContact() {
		MergingRegister r1 = new MergingRegister(0);
		MergingRegister r2 = new MergingRegister(1);

		SprayMessage m = new SprayMessage(null, r1.networks, r1.size);

		assertTrue(r2.waiting.isEmpty());
		assertTrue(r2.newNetworkDetected(m, 1));
		assertFalse(r2.waiting.isEmpty());

		SprayMessage n = new SprayMessage(null, r2.networks, r2.size);

		assertTrue(r1.waiting.isEmpty());
		assertTrue(r1.newNetworkDetected(n, 1));
		assertFalse(r1.waiting.isEmpty());
	}

	// [{0}] -> [{0}]
	public void testFirstSelfContact() {
		MergingRegister r1 = new MergingRegister(0);
		MergingRegister r2 = new MergingRegister(0);

		SprayMessage m = new SprayMessage(null, r1.networks, -1);

		assertTrue(r2.waiting.isEmpty());
		assertFalse(r2.newNetworkDetected(m, 1));
		assertTrue(r2.waiting.isEmpty());
	}

	// (([{0}] -> [{1}]) -> [{2}]) -> [{0}]
	public void testMultipleNetworksTransitively() {
		MergingRegister r1 = new MergingRegister(0);
		MergingRegister r2 = new MergingRegister(1);
		MergingRegister r3 = new MergingRegister(2);

		SprayMessage m = new SprayMessage(null, r1.networks, -1);

		r2.newNetworkDetected(m, 1);

		SprayMessage n = new SprayMessage(null, r2.networks, -1);

		assertTrue(r3.newNetworkDetected(n, 1));
		assertTrue(r3.waiting.size() == 2);
		assertTrue(r3.networks.getFirst().size() == 2);
		assertTrue(r3.flattenNetworks.size() == 3);

		SprayMessage o = new SprayMessage(null, r3.networks, -1);

		assertTrue(r1.newNetworkDetected(o, 1));
		assertTrue(r1.waiting.size() == 2);
		assertTrue(r1.networks.getFirst().size() == 2);
		assertTrue(r1.flattenNetworks.size() == 3);
	}

	// [{0}] -> [{1}]
	public void testKeepSizeFirstContact() {
		MergingRegister r1 = new MergingRegister(0);
		MergingRegister r2 = new MergingRegister(1);

		SprayMessage m = new SprayMessage(null, r1.networks, 4);

		r2.newNetworkDetected(m, 1);
		assertTrue(r2.keepSize(m));
		assertTrue(r2.got.get(r2.networks.getFirst()) == 4);
	}

	// [{0}] -> [{0}]
	public void testKeepSizeSameNetwork() {
		MergingRegister r1 = new MergingRegister(0);
		MergingRegister r2 = new MergingRegister(0);

		SprayMessage m = new SprayMessage(null, r1.networks, 4);

		r2.newNetworkDetected(m, 1);
		assertFalse(r2.keepSize(m));
		assertTrue(r2.got.isEmpty());
	}

	// ([{0}] -> [{1}]) -> [{2}]
	public void testKeepSizeNetworkTranstively() {
		MergingRegister r1 = new MergingRegister(0);
		MergingRegister r2 = new MergingRegister(1);
		MergingRegister r3 = new MergingRegister(2);

		SprayMessage m = new SprayMessage(null, r1.networks, 1);

		r2.newNetworkDetected(m, 1);
		assertTrue(r2.keepSize(m));
		assertEquals(1, r2.got.size());

		SprayMessage n = new SprayMessage(null, r2.networks, 2);

		r3.newNetworkDetected(n, 1);
		assertTrue(r3.keepSize(n));
		assertEquals(1, r3.got.size());
		HashSet<Integer> key = r3.got.keySet().iterator().next();
		assertTrue(2 == r3.got.get(key));
	}

	public void testPermutations1() {
		HashSet<Integer> got1 = new HashSet<Integer>();
		got1.add(1);
		HashSet<Integer> got2 = new HashSet<Integer>();
		got2.add(2);
		got2.add(3);
		HashSet<Integer> got3 = new HashSet<Integer>();
		got3.add(3);
		HashSet<Integer> got4 = new HashSet<Integer>();
		got4.add(3);
		got4.add(4);

		MergingRegister r = new MergingRegister();
		r.waiting.add(1);
		r.waiting.add(2);
		r.waiting.add(3);
		r.waiting.add(4);
		r.got.put(got1, 1);
		r.got.put(got2, 2);
		r.got.put(got3, 3);
		r.got.put(got4, 4);

		HashSet<HashSet<Integer>> result = r.permutations(
				(HashSet<Integer>) r.waiting.clone(),
				new HashSet<HashSet<Integer>>());
		assertNull(result);
	}

	public void testPermutations2() {
		HashSet<Integer> got1 = new HashSet<Integer>();
		got1.add(1);
		HashSet<Integer> got2 = new HashSet<Integer>();
		got2.add(2);
		got2.add(3);
		HashSet<Integer> got3 = new HashSet<Integer>();
		got3.add(3);
		HashSet<Integer> got4 = new HashSet<Integer>();
		got4.add(3);
		got4.add(4);
		HashSet<Integer> got5 = new HashSet<Integer>();
		got5.add(2);

		MergingRegister r = new MergingRegister();
		r.waiting.add(1);
		r.waiting.add(2);
		r.waiting.add(3);
		r.waiting.add(4);
		r.got.put(got1, 1);
		r.got.put(got2, 2);
		r.got.put(got3, 3);
		r.got.put(got4, 4);
		r.got.put(got5, 5);

		HashSet<HashSet<Integer>> result = r.permutations(
				(HashSet<Integer>) r.waiting.clone(),
				new HashSet<HashSet<Integer>>());
		assertNotNull(result);
	}

	public void testPermutation3() {
		HashSet<Integer> got1 = new HashSet<Integer>();
		got1.add(5);
		HashSet<Integer> got2 = new HashSet<Integer>();
		got2.add(0);
		got2.add(1);
		got2.add(2);
		got2.add(3);
		HashSet<Integer> got3 = new HashSet<Integer>();
		got3.add(0);
		got3.add(1);
		got3.add(2);
		got3.add(4);
		HashSet<Integer> got4 = new HashSet<Integer>();
		got4.add(7);
		HashSet<Integer> got5 = new HashSet<Integer>();
		got5.add(4);
		got5.add(3);
		HashSet<Integer> got6 = new HashSet<Integer>();
		got6.add(0);
		got6.add(1);
		got6.add(3);
		got6.add(4);
		HashSet<Integer> got7 = new HashSet<Integer>();
		got7.add(8);
		HashSet<Integer> got8 = new HashSet<Integer>();
		got8.add(1);
		got8.add(3);
		got8.add(4);
		HashSet<Integer> got9 = new HashSet<Integer>();
		got9.add(0);
		got9.add(2);
		got9.add(3);
		got9.add(4);
		HashSet<Integer> got10 = new HashSet<Integer>();
		got10.add(9);
		HashSet<Integer> got11 = new HashSet<Integer>();
		got11.add(2);
		got11.add(3);
		got11.add(4);
		HashSet<Integer> got12 = new HashSet<Integer>();
		got12.add(1);
		got12.add(2);
		got12.add(3);
		got12.add(4);

		MergingRegister r = new MergingRegister();
		r.waiting.add(0);
		r.waiting.add(1);
		r.waiting.add(2);
		r.waiting.add(3);
		r.waiting.add(4);
		r.waiting.add(5);
		r.waiting.add(7);
		r.waiting.add(8);
		r.waiting.add(9);
		r.got.put(got1, 1);
		r.got.put(got2, 2);
		r.got.put(got3, 3);
		r.got.put(got4, 4);
		r.got.put(got5, 5);
		r.got.put(got6, 5);
		r.got.put(got7, 5);
		r.got.put(got8, 5);
		r.got.put(got9, 5);
		r.got.put(got10, 5);
		r.got.put(got11, 5);
		r.got.put(got12, 5);

		System.out.println(r.toString());
		HashSet<HashSet<Integer>> result = r.permutations(
				(HashSet<Integer>) r.waiting.clone(),
				new HashSet<HashSet<Integer>>());
		assertNull(result);
	}

	public void testIsMerge1() {
		MergingRegister r1 = new MergingRegister(0);
		MergingRegister r2 = new MergingRegister(1);

		SprayMessage m = new SprayMessage(null, r1.networks, 1);

		assertEquals(0.69, r2.isMerge(m, 1), 0.1);
	}
}
