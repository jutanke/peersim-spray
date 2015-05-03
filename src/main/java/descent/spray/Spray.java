package descent.spray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import descent.Dynamic;
import descent.scamp.Scamp;

/**
 * THIS is not EVENT-based due to simplification Created by julian on 3/31/15.
 */
public class Spray extends SprayProtocol implements Dynamic, PartialView.Parent {

	private static final String PARAM_START_SHUFFLE = "startShuffle";
	private static final String PAR_FAILURE = "failure";
	// ============================================
	// E N T I T Y
	// ============================================

	protected PartialView partialView;
	protected Map<Long, Node> inView;
	private boolean isUp = true;
	protected static final int FORWARD_TTL = 125;
	private static double failure;
	protected final int startShuffle;
	protected int callCount = 0;
	protected long lastCycle = Long.MIN_VALUE;
	protected boolean useUnsubscription = false;

	public Spray(String prefix) {
		super(prefix);
		this.startShuffle = Configuration.getInt(prefix + "."
				+ PARAM_START_SHUFFLE, 0);
		Spray.failure = Configuration.getDouble(prefix + "." + PAR_FAILURE, 0);
		this.partialView = new PartialView();
		this.inView = new HashMap<Long, Node>();
	}

	@Override
	public Object clone() {
		Spray s = (Spray) super.clone();
		s.partialView = new PartialView();
		s.inView = new HashMap<Long, Node>();
		return s;
	}

	// ============================================
	// P U B L I C
	// ============================================

	public void nextCycle(Node node, int protocolID) {
		if (this.isUp()) {
			this.startShuffle(node);
		}
	}

	public boolean isUp() {
		return this.isUp;
	}

	public void up() {
		this.isUp = true;
	}

	public void down() {
		this.isUp = false;
	}

	public int hash() {
		return 0;
	}

	public void processEvent(Node node, int pid, Object event) {
		// N E V E R U S E D
	}

	public int degree() {
		return this.partialView.degree();
	}

	public Node getNeighbor(int i) {
		return this.partialView.get(i);
	}

	public boolean addNeighbor(Node neighbour) {
		return this.partialView.add(neighbour);
	}

	public boolean contains(Node neighbor) {
		return this.partialView.contains(neighbor);
	}

	public List<Node> getPeers() {
		return this.partialView.list();
	}

	public String debug() {
		StringBuilder sb = new StringBuilder();
		sb.append(", in:[");
		for (Node n : this.inView.values()) {
			if (sb.length() > 4) {
				sb.append(",");
			}
			sb.append(n.getID());
		}
		sb.append("], out:");
		sb.append(this.partialView);
		sb.append(", isUp:");
		sb.append(this.isUp());
		return sb.toString();
	}

	public int callsInThisCycle() {
		return this.callCount;
	}

	public void clearCallsInCycle() {
		this.callCount = 0;
	}

	/**
	 * process if the connection has failed
	 * 
	 * @return true if the connection has failed, false otherwise
	 */
	private boolean pF() {
		return CommonState.r.nextDouble() < (1 - Math.pow(1 - Spray.failure, 6));
	}

	// ============================================
	// C Y C L I C
	// ============================================

	/**
	 * A* --> B
	 *
	 * @param me
	 */
	public void startShuffle(Node me) {

		final long currentTime = CommonState.getTime();
		if (currentTime > this.lastCycle) {
			this.callCount = 0;
		}

		this.updateInView(me);
		if (this.isUp() && this.startShuffle <= CommonState.getTime()) {
			if (this.degree() > 0) {
				this.partialView.freeze();
				this.partialView.incrementAge();
				final PartialViewEntry q = this.partialView.oldest();
				final List<PartialViewEntry> nodesToSend = this.partialView
						.subsetMinus1(q);
				nodesToSend.add(new PartialViewEntry(me));
				final Spray Q = (Spray) q.node.getProtocol(pid);
				if (Q.isUp()) {
					Q.receiveShuffle(q.node, me,
							PartialView.clone(nodesToSend), this.degree());
				} else {
					// TIME OUT
					final double p = (double) (c + 1)
							/ this.partialView.degree();
					final int count = this.partialView.deleteAll(q.node);
					this.inView.remove(q.node.getID());
					if (this.partialView.degree() > 0) {
						// recreate a link:
						if (this.partialView.degree() > 0) {
							for (int i = 0; i < count; i++) {
								if (CommonState.r.nextDouble() > p) {
									final Node r = this.partialView
											.get(CommonState.r
													.nextInt(this.partialView
															.degree()));
									this.partialView.addMultiset(r);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * A --> B*
	 *
	 * @param me
	 * @param sender
	 * @param received
	 * @param otherPartialViewSize
	 */
	public void receiveShuffle(final Node me, final Node sender,
			final List<PartialViewEntry> received,
			final int otherPartialViewSize) {
		if (this.isUp()) {
			this.callCountPP(me, sender);
			this.partialView.freeze();
			List<PartialViewEntry> nodesToSend = this.partialView.subset();
			final int size = this.degree();
			// .println("receive shuffle @" + me.getID() + " from " +
			// sender.getID());
			// System.err.println("Send nodes: " + nodesToSend);
			this.partialView.merge(me, sender, received, otherPartialViewSize,
					false);
			this.updateInView(me);
			this.updateOutView(me);
			final Spray P = (Spray) sender.getProtocol(pid);

			P.finishShuffle(sender, me, PartialView.clone(nodesToSend), size);
		}
	}

	/**
	 * A* --> B
	 *
	 * @param me
	 * @param sender
	 * @param received
	 *            (FROM B)
	 * @param otherPartialViewSize
	 */
	public void finishShuffle(final Node me, final Node sender,
			final List<PartialViewEntry> received,
			final int otherPartialViewSize) {
		if (this.isUp()) {
			// System.err.println("finish shuffle @" + me.getID() + " from " +
			// sender.getID());
			this.partialView.merge(me, sender, received, otherPartialViewSize);
			this.updateInView(me);
			this.updateOutView(me);
		}
	}

	// ============================================
	// S C A M P
	// ============================================

	/**
	 * Transform a --> (me) --> b into a --> b
	 *
	 * @param node
	 */
	public static void unsubscribe(Node node) {
		final Spray current = (Spray) node.getProtocol(pid);
		if (!current.useUnsubscription)
			return;

		current.updateInView(node);
		// System.err.println(current.debug());
		if (current.isUp()) {
			int count = 0;
			current.down();
			final int ls = current.inView.size();
			final int notifyIn = Math.max(ls - c - 1, 0);
			final Queue<Node> in = new LinkedList<Node>(current.inView.values());
			final List<Node> out = current.partialView.list();
			for (int i = 0; i < notifyIn && out.size() > 0; i++) {
				final Node a = in.poll();
				final Node b = out.get(i % out.size());
				count += current.replace(node, a, b);
			}

			while (!in.isEmpty()) {
				final Spray next = (Spray) in.poll().getProtocol(pid);
				count += next.partialView.deleteAll(node);
			}
			System.err.println("remove " + node.getID() + ", delete " + count
					+ " arcs");
			current.partialView.clear();
			current.inView.clear();
		}
	}

	/**
	 * SUBSCRIBE
	 *
	 * @param s
	 * @param c
	 */
	public static void subscribe(final Node s, final Node c) {
		final Spray subscriber = (Spray) s.getProtocol(pid);
		subscriber.inView.clear();
		subscriber.partialView.clear();

		final Spray contact = (Spray) c.getProtocol(pid);

		if (subscriber.isUp() && contact.isUp()) {

			subscriber.addNeighbor(c);
			for (Node n : contact.getPeers()) {
				insert(s, n);
			}

			for (int i = 0; i < Spray.c && contact.degree() > 0; i++) {
				final Node n = contact.getNeighbor(CommonState.r
						.nextInt(contact.degree()));
				insert(s, n);
			}

		} else {
			throw new RuntimeException("@Subscribe (" + s.getID() + " -> "
					+ c.getID() + " not up");
		}
		// System.err.println("add " + s.getID() + ", add " + count + " arcs");
	}

	private static boolean insert(Node s, Node n) {
		final Spray subscriber = (Spray) s.getProtocol(pid);
		if (n.getID() != s.getID()) {
			final Spray current = (Spray) n.getProtocol(pid);
			if (current.isUp()) {
				current.addNeighbor(s);
				subscriber.addToInview(s, n);
				return true;
			}
		}
		return false;
	}


	// =================================================================
	// H E L P E R
	// =================================================================

	private void callCountPP(Node me, Node from) {
		final long currentTime = CommonState.getTime();

		if (this.lastCycle == currentTime) {
			this.callCount += 1;
		} else if (this.lastCycle < currentTime) {
			this.callCount = 1;
		} else {
			throw new RuntimeException("nope..99");
		}
		this.lastCycle = currentTime;
	}

	protected void updateInView(Node me) {
		List<Node> in = new ArrayList<Node>(this.inView.values());
		for (Node n : in) {
			final Spray current = (Spray) n.getProtocol(pid);
			if (!current.isUp() || !current.contains(me)) {
				this.inView.remove(n.getID());
			}
		}
	}

	protected void updateOutView(Node me) {
		for (Node n : this.getPeers()) {
			final Spray current = (Spray) n.getProtocol(pid);
			if (!current.inView.containsKey(me.getID())) {

				current.addToInview(n, me);
			}
		}
	}

	/**
	 * Turn a --> (me) --> b Into a --> b
	 *
	 * @param me
	 * @param a
	 * @param b
	 * @return
	 */
	private int replace(Node me, Node a, Node b) {
		int count = this.partialView.count(b); // not really accurate
		final Spray A = (Spray) a.getProtocol(pid);
		final Spray B = (Spray) b.getProtocol(pid);
		if (me.getID() == a.getID() || me.getID() == b.getID()) {
			throw new RuntimeException("FATAL");
		}
		if (a.isUp() && b.isUp()) {
			if (a.getID() == b.getID()) {
				count += A.partialView.deleteAll(me);
				B.inView.remove(me.getID());
			} else {
				final int swn = A.partialView.switchNode(me, b);
				count += Math.max(swn / 2, 1); // inaccurate
				B.inView.remove(me.getID());
				if (!B.inView.containsKey(a.getID())) {
					B.addToInview(b, a);
				}
			}
		} else {
			// either a or b is down, so we just kill all links regarding (me)
			count += A.partialView.deleteAll(me);
			B.inView.remove(me.getID());
		}
		return count;
	}

	protected void addToInview(Node me, Node n) {
		if (me.getID() == n.getID()) {
			throw new RuntimeException("cannot put myself");
		}
		if (!this.inView.containsKey(n.getID())) {
			this.inView.put(n.getID(), n);
		}
	}

}