package descent.scamplon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import peersim.cdsim.CDState;
import peersim.core.Node;

/**
 * Created by julian on 2/5/15.
 */
public class PartialView {

	public static boolean TEST_ENV = false;
	private static final Random r = new Random(42);

	// ============================================
	// E N T I T Y
	// ============================================

	// pointer to the own node
	// private final Node self;
	public List<PartialViewEntry> out;
	private List<Node> list;

	public PartialView() {
		// this.self = self;
		this.list = new ArrayList<Node>();
		this.out = new ArrayList<PartialViewEntry>();
	}

	// ============================================
	// P U B L I C
	// ============================================

	/**
	 * @return
	 */
	public List<Node> list() {
		this.list.clear();
		for (PartialViewEntry e : this.out) {
			this.list.add(e.node);
		}
		return this.list;
	}

	/**
	 * @param n
	 * @return
	 */
	public boolean add(Node n) {
		if (this.contains(n)) {
			return false;
		}
		this.out.add(new PartialViewEntry(n));
		return true;
	}

	public void addMultiset(Node n) {
		this.out.add(new PartialViewEntry(n));
	}

	public boolean delete(PartialViewEntry e) {
		int i = 0;
		boolean found = false;
		for (; i < this.out.size(); i++) {
			if (this.out.get(i) == e) {
				found = true;
				break;
			}
		}
		if (found) {
			this.out.remove(i);
			return true;
		}
		return false;
	}

	public int deleteAll(Node n) {
		int count = 0;
		while (this.contains(n)) {
			int i = 0;
			for (; i < this.degree(); i++) {
				if (this.get(i).getID() == n.getID()) {
					this.out.remove(i);
					count += 1;
					break;
				}
			}
		}
		return count;
	}

	public void clear() {
		this.out.clear();
	}

	public int count(Node n) {
		int i = 0;
		for (PartialViewEntry e : this.out) {
			if (e.node.getID() == n.getID()) {
				i++;
			}
		}
		return i;
	}

	public int switchNode(Node oldNode, Node newNode) {
		int count = 0;
		for (PartialViewEntry e : this.out) {
			if (e.node.getID() == oldNode.getID()) {
				e.node = newNode;
				count += 1;
			}
		}
		return count;
	}

	/**
	 * @param n
	 * @return
	 */
	public boolean contains(Node n) {
		return contains(this.out, n);
	}

	/**
	 * @return size of the partial view
	 */
	public int degree() {
		return this.out.size();
	}

	/**
	 * Needed for the interface
	 *
	 * @param i
	 * @return
	 */
	public Node get(int i) {
		return this.out.get(i).node;
	}

	/**
	 * might be needed internally
	 *
	 * @param n
	 * @return
	 */
	public List<PartialViewEntry> get(Node n) {
		return get(this.out, n);
	}

	/**
	 * Of all entries
	 */
	public void incrementAge() {
		for (PartialViewEntry e : this.out) {
			e.age += 1;
		}
	}

	/**
	 * @return subset-size
	 */
	public int l() {
		return (int) Math.max(1, Math.ceil(this.degree() / 2.0));
	}

	/**
	 * should be used in the shuffle-response part
	 *
	 * @return
	 */
	public List<PartialViewEntry> subset() {
		this.freeze();
		return subset(this.out, this.l());
	}

	/**
	 * Should be used in the shuffle code
	 *
	 * @param oldest
	 * @return
	 */
	public List<PartialViewEntry> subsetMinus1(PartialViewEntry oldest) {
		return subset(this.out, oldest, this.l() - 1);
	}

	/**
	 * @return oldest element
	 */
	public PartialViewEntry oldest() {
		if (this.out.size() == 0)
			return null;
		PartialViewEntry oldest = this.out.get(0);
		for (PartialViewEntry e : this.out) {
			if (oldest.age < e.age) {
				oldest = e;
			}
		}
		oldest.isVolatile = true;
		return oldest;
	}

	public void freeze() {
		for (PartialViewEntry e : this.out) {
			e.isVolatile = false;
		}
	}

	public void merge(Node self, Node oldest, List<PartialViewEntry> received,
			int otherSize) {
		this.out = merge(self, oldest, this.out, received, otherSize);
	}

	public void merge(Node self, Node oldest, List<PartialViewEntry> received,
			int otherSize, final boolean FROM_SENDER) {
		this.out = merge(self, oldest, this.out, received, otherSize,
				FROM_SENDER);
	}

	public boolean p() {
		return CDState.r.nextDouble() < 1.0 / (1.0 + this.out.size());
	}

	public void unhassle() {
		for (PartialViewEntry e : this.out) {
			e.isVolatile = false;
		}
	}

	// ============================================
	// P R I V A T E
	// ============================================

	// ============================================
	// L I S T H E L P E R
	// ============================================

	public static List<PartialViewEntry> merge(final Node me, final Node other,
			final List<PartialViewEntry> List, List<PartialViewEntry> received,
			final int otherSize) {
		return merge(me, other, List, received, otherSize, true);
	}

	public static List<PartialViewEntry> merge(final Node me, final Node other,
			final List<PartialViewEntry> List, List<PartialViewEntry> received,
			int otherSize, final boolean FROM_SENDER) {

		// System.err.println("@" + me.getID() + " <- " + other.getID() + " pv:"
		// + list + " rec:" + received + " othersize:" + otherSize);

		// Scamplon culprit = (Scamplon) other.getProtocol(Scamplon.pid);
		// System.err.println("culprit " +other.getID()+ " :" + culprit);

		// System.err.println("list size:" + list.size() + " (@" + me.getID() +
		// ")");

		int newSize = (List.size() % 2 == 0) ? (int) Math
				.ceil((List.size() + otherSize) / 2.0) : (int) Math.floor((List
				.size() + otherSize) / 2.0);

		// System.err.println("(" + list.size() + " + " + otherSize + ")/ 2 = "
		// + newSize);

		Parent qq = (Parent) me.getProtocol(ScamplonProtocol.pid);

		// System.err.println("from " + other.getID() + " get " + received +
		// " @" + me.getID() + " = " + qq.debug() + " sender:" + FROM_SENDER +
		// " otherSize:" + otherSize);

		// System.err.println("NOW:" + List + " @" + me.getID() + " from " +
		// other.getID());

		RemoveVolatileResult rem = removeVolatileResults(List);
		List<PartialViewEntry> list = rem.rest;

		if (contains(received, me)) {
			List<PartialViewEntry> sent = removeAll(rem.volatiles, me); // here
																		// will
																		// never
			// remove any
			// element!
			// if (FROM_SENDER) {
			sent = removeAll(sent, other);
			// }
			int itemsRemoved = 0;
			int sizeBefore = received.size();
			received = removeAll(received, me); // here we might remove possibly
												// more elements..
			itemsRemoved += (sizeBefore - received.size());
			for (int i = 0; i < itemsRemoved; i++) {
				if (sent.size() > 0) {
					received.add(popYoungest(sent));
				} else {
					received.add(new PartialViewEntry(other)); // introduce a
																// new arc!
					// Because we removed one
					// arc before!
				}
			}
		}

		if (newSize != (list.size() + received.size())) {
			System.err.println("Error @" + me.getID() + " receiving from "
					+ other.getID() + " is sender:" + FROM_SENDER);
			System.err.println("@" + me.getID() + " = " + qq.debug());
			System.err.println("orig " + List + " =vs= " + list + " otherSize:"
					+ otherSize);
			System.err.println(newSize + " vs " + list.size() + " + "
					+ received.size());
			// System.err.println("from " + other.getID() + " rec:" + received +
			// " isup:" +
			// ((Scamplon)other.getProtocol(Scamplon.pid)).isUp());
			throw new RuntimeException("@" + me.getID()
					+ ":LOSING ARCS! MUST NOT HAPPEN!");
		}

		list.addAll(received);
		for (PartialViewEntry e : list) {
			e.isVolatile = false;
		}
		return list;
	}

	/**
	 * Select a random integer between max and 0
	 *
	 * @param max
	 * @return
	 */
	private static int nextInt(int max) {
		if (!TEST_ENV) {
			return CDState.r.nextInt(max);
		} else {
			return r.nextInt(max);
		}
	}

	private static PartialViewEntry popYoungest(List<PartialViewEntry> list) {
		if (list.size() == 0) {
			return null;
		}

		PartialViewEntry youngest = youngest(list);
		list.remove(youngest);
		return youngest;
	}

	private static PartialViewEntry youngest(List<PartialViewEntry> list) {
		PartialViewEntry youngest = list.get(0);
		for (PartialViewEntry e : list) {
			if (e.age < youngest.age) {
				youngest = e;
			}
		}
		return youngest;
	}

	/**
	 * @param list
	 * @param l
	 * @return
	 */
	public static List<PartialViewEntry> subset(List<PartialViewEntry> list,
			int l) {
		return subset(list, null, l);
	}

	/**
	 * @param list
	 * @param filter
	 * @param l
	 * @return
	 */
	public static List<PartialViewEntry> subset(List<PartialViewEntry> list,
			PartialViewEntry filter, int l) {
		List<PartialViewEntry> res = clone(list);
		if (filter != null) {
			res = remove(res, filter);
		}
		if (l >= res.size()) {
			List<PartialViewEntry> result = new ArrayList<PartialViewEntry>();
			for (PartialViewEntry e : res) {
				PartialViewEntry newE = e.clone();
				e.isVolatile = true;
				result.add(newE);
			}
			return result;
		} else {
			HashSet<Integer> pos = new HashSet<Integer>();
			for (int i = 0; i < l; i++) {
				int p = nextInt(res.size());
				while (pos.contains(p)) {
					p = nextInt(res.size()); // resolves eventually
				}
				pos.add(p);
			}
			List<PartialViewEntry> result = new ArrayList<PartialViewEntry>();
			for (int i : pos) {
				PartialViewEntry current = res.get(i);
				result.add(current.clone());
				current.isVolatile = true;
			}
			return result;
		}
	}

	/**
	 * Checks if the list contains an entry with said node
	 *
	 * @param list
	 * @param n
	 * @return
	 */
	public static boolean contains(List<PartialViewEntry> list, Node n) {
		for (PartialViewEntry e : list) {
			if (e.node.getID() == n.getID()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Delivers the node or null
	 *
	 * @param list
	 * @param n
	 * @return
	 */
	public static List<PartialViewEntry> get(List<PartialViewEntry> list, Node n) {
		List<PartialViewEntry> result = new ArrayList<PartialViewEntry>();
		for (PartialViewEntry e : list) {
			if (n.getID() == e.node.getID()) {
				result.add(e);
			}
		}
		return result;
	}

	public static List<PartialViewEntry> removeAll(List<PartialViewEntry> list,
			Node n) {
		List<PartialViewEntry> del = get(list, n);
		for (PartialViewEntry e : del) {
			list.remove(e);
		}
		return list;
	}

	/**
	 * Remove an element
	 *
	 * @param list
	 * @param n
	 * @return
	 */
	public static List<PartialViewEntry> remove(List<PartialViewEntry> list,
			PartialViewEntry n) {
		List<PartialViewEntry> del = get(list, n.node);
		for (PartialViewEntry e : del) {
			if (e.age == n.age) {
				list.remove(e);
				break;
			}
		}
		/*
		 * if (del != null) { list.remove(del); }
		 */
		return list;
	}

	/**
	 * Clones a list
	 *
	 * @param list
	 * @return
	 */
	public static List<PartialViewEntry> clone(List<PartialViewEntry> list) {
		List<PartialViewEntry> result = new ArrayList<PartialViewEntry>();
		for (PartialViewEntry e : list) {
			result.add(e);
		}
		return result;
	}

	/**
	 * @param list
	 * @return
	 */
	public static List<PartialViewEntry> sort(List<PartialViewEntry> list) {
		Arrays.sort(list.toArray(new PartialViewEntry[0]),
				new PartialViewEntry(null));
		return list;
	}

	/**
	 * Splits up the view
	 *
	 * @param list
	 * @return
	 */
	/*
	 * public static List<Entry> removeVolatileResults(List<Entry> list) {
	 * List<Entry> rest = clone(list); for (Entry e : list) { if (e.isVolatile)
	 * { e.isVolatile = false; rest.remove(e); } } return rest; }
	 */

	/**
	 * Splits up the view
	 *
	 * @return
	 */
	private static RemoveVolatileResult removeVolatileResults(
			List<PartialViewEntry> list) {
		List<PartialViewEntry> rest = clone(list);
		List<PartialViewEntry> volatiles = new ArrayList<PartialViewEntry>();
		for (PartialViewEntry e : list) {
			if (e.isVolatile) {
				volatiles.add(e);
				e.isVolatile = false;
				rest.remove(e);
			}
		}
		return new RemoveVolatileResult(volatiles, rest);
	}

	@Override
	public String toString() {
		return " -> " + this.out.toString();
	}

	// ============================================
	// R E M O V E _ V O L A T I L E
	// ============================================
	private static final class RemoveVolatileResult {
		public final List<PartialViewEntry> volatiles;
		public final List<PartialViewEntry> rest;

		public RemoveVolatileResult(List<PartialViewEntry> v,
				List<PartialViewEntry> r) {
			this.volatiles = v;
			this.rest = r;
		}
	}

	// ============================================
	// P A R E N T
	// ============================================

	public interface Parent {
		String debug();
	}

}
