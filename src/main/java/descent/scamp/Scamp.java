package descent.scamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import descent.Dynamic;
import descent.PeerSamplingService;

/**
 * ATTENTION: The lease-mechanism suggested in the Paper
 * "Peer-to-Peer Membership Management for Gossip-Based Protocols" from
 * A.J. Ganesh, A-M. Kermarrec and L. Massoulie seems to be broken as the
 * arc count grows until the network is almost complete
 *
 * Inspired by https://github.com/csko/Peersim/tree/master/scamp
 *
 * Created by julian on 4/9/15.
 */
public class Scamp implements CDProtocol, Dynamic, Linkable,
		PeerSamplingService {

	private static final String PAR_C = "c";
	public static final String SCAMP_PROT = "0";
	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_FAILURE = "failure";
	private static final String PAR_LEASE_MAX = "leaseTimeoutMax";
	private static final String PAR_LEASE_MIN = "leaseTimeoutMin";

	// ===========================================
	// P R O P E R T I E S
	// ===========================================

	public List<Node> in;
	public List<Node> out;
	private ViewEntry current;

	private static final int FORWARD_TTL = 125;
	private static double failure;
	public static int pid;
	public static int tid;
	public static int c;
	public final int MAX_LEASE;
	public final int MIN_LEASE;
	private boolean isUp;
	private int interval;

	// ===========================================
	// C T O R
	// ===========================================

	public Scamp(String n) {
		Scamp.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
		Scamp.c = Configuration.getInt(n + "." + PAR_C, 0);
		Scamp.failure = Configuration.getDouble(n + "." + PAR_FAILURE, 0);
		Scamp.pid = Configuration.lookupPid(SCAMP_PROT);
		this.MAX_LEASE = Configuration.getInt(n + "." + PAR_LEASE_MAX, 2000);
		this.MIN_LEASE = Configuration.getInt(n + "." + PAR_LEASE_MIN, 1000);
		this.in = new ArrayList<Node>();
		this.out = new ArrayList<Node>();
		this.interval = CommonState.r.nextInt(this.MAX_LEASE - this.MIN_LEASE)
				+ this.MIN_LEASE;
	}

	@Override
	public Object clone() {
		Scamp scamp = null;
		try {
			scamp = (Scamp) super.clone();
			scamp.in = new ArrayList<Node>();
			scamp.out = new ArrayList<Node>();
			scamp.interval = CommonState.r.nextInt(this.MAX_LEASE
					- this.MIN_LEASE)
					+ this.MIN_LEASE;
		} catch (CloneNotSupportedException e) {
		} // never happens
			// ...
		return scamp;
	}

	// ===========================================
	// P U B L I C
	// ===========================================

	public void nextCycle(Node node, int protocolID) {
		if (this.isUp()) {
			// this.leaseOthers();
			if (this.isTimedOut()) {
				lease(node);
			}
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

	public int degree() {
		return this.out.size();
	}

	public Node getNeighbor(int i) {
		return this.out.get(i);
	}

	public boolean addNeighbor(Node neighbour) {
		if (this.out.contains(neighbour)) {
			return false;
		} else {
			this.out.add(neighbour);
			return true;
		}
	}

	public boolean contains(Node neighbor) {
		return this.out.contains(neighbor);
	}

	public void pack() {

	}

	public void onKill() {

	}

	public List<Node> getPeers() {
		final List<Node> result = new ArrayList<Node>(this.out);
		return result;
	}

	public String debug() {
		return "{in:" + this.in + ", out:" + this.out + "}";
	}

	public int callsInThisCycle() {
		return 0;
	}

	public void clearCallsInCycle() {

	}

	// ===========================================
	// P R I V A T E
	// ===========================================

	private void leaseOthersX() {
		outer: while (true) {
			for (int i = 0; i < this.out.size(); i++) {
				final Scamp N = (Scamp) this.out.get(i).getProtocol(pid);
				if (N.isTimedOut()) {
					this.out.remove(i);
					break outer;
				}
			}
			break;
		}
	}

	private boolean isTimedOut() {
		return CommonState.getTime() > 0
				&& CommonState.getTime() % this.interval == 0;
	}

	private boolean p() {
		return CommonState.r.nextDouble() < 1.0 / (1.0 + this.degree());
	}

	/**
	 * process if the connection has failed
	 * 
	 * @param path
	 *            the ordered list of node that the message traveled through
	 * @return true if the connection has failed, false otherwise
	 */
	private boolean pF(List<Node> path) {
		// Graph of the path
		final Map<Long, HashSet<Long>> graph = new HashMap<Long, HashSet<Long>>();
		for (int i = 0; i < path.size(); ++i) {
			final Node n = path.get(i);
			if (!graph.containsKey(n.getID())) {
				graph.put(n.getID(), new HashSet<Long>());
			}
			if (0 < i) {
				final Node pre = path.get(i - 1);
				graph.get(pre.getID()).add(n.getID());
			}
		}
		HashSet<Long> discovered = new HashSet<Long>();
		// BFS measuring shortest path length
		Queue<Long> q = new LinkedList<Long>();
		q.add(path.get(0).getID());
		discovered.add(path.get(0).getID());
		int minHops = 0;
		while (!q.isEmpty()) {
			++minHops;
			final Long current = q.poll();
			if (current == path.get(path.size() - 1).getID()) {
				break; // ugly break
			}
			for (Long neighbor : graph.get(current)) {
				if (!discovered.contains(neighbor)) {
					q.add(neighbor);
					discovered.add(neighbor);
				}
			}
		}
		/*
		 * System.out.println("===Path==="); for (int i= 0; i<path.size();++i){
		 * System.out.print(path.get(i).getID()+" - "); } System.out.println();
		 * System.out.println("minHops = " + minHops);
		 */
		// minHops = path.size(); // worst case
		double p = Math.pow(1 - Scamp.failure, Math.pow(minHops, 2) + 3
				* minHops + 2);
		return CommonState.r.nextDouble() < (1 - p);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("out:[");
		for (Node n : this.out) {
			sb.append(n.getID());
			sb.append(" ");
		}
		sb.append("] in:[");
		for (Node n : this.in) {
			sb.append(n.getID());
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}

	// ===========================================
	// P R O T O C O L
	// ===========================================

	public static void lease(final Node n) {
		final Scamp N = (Scamp) n.getProtocol(pid);

		// remove yourself from everyone pointing to you!
		int cutNumber = N.in.size();
		for (Node in : N.in) {
			final Scamp In = (Scamp) in.getProtocol(pid);
			boolean found = false;
			int i = 0;
			while (!found && i < In.out.size()) {
				final Node o = In.out.get(i);
				if (o.getID() == n.getID()) {
					In.out.remove(i);
					found = true;
				}
				++i;
			}
		}

		// resubscribe
		N.in.clear();
		// Node c = Network.get(CommonState.r.nextInt(Network.size()));
		// if (N.degree() > 0) {
		// c = N.out.get(CommonState.r.nextInt(N.degree()));
		// }
		// subscribe(n, c, true);
		// System.err.println("LEASE @" + n.getID() + " " + N.toString()
		// + ", select c=" + c.getID());

		// It has neighbours in its partial view so he can try with them
		// may not cut any arc though, mi no kno Y :$, eventually fall off in
		// the third case
		if (N.degree() > 0) {
			boolean performed = false;
			int tries = 10;
			while (!performed && tries > 0) {
				Node c = N.out.get(CommonState.r.nextInt(N.degree()));
				performed = alternativeLease(n, c, cutNumber);
				tries -= 1;
			}
		}
		// It has no neighbours, it tries with a random one in the network
		if (N.degree() == 0 && cutNumber > 0) {
			boolean performed = false;
			int tries = 10;
			while (!performed && tries > 0) {
				Node c = Network.get(CommonState.r.nextInt(Network.size()));
				performed = alternativeLease(n, c, cutNumber);
				tries -= 1;
			}
		}
		// No neighbours at all, i.e., no inview, no partial view
		// if (N.degree() == 0 && cutNumber == 0) {
		// System.out.println("AFHAZOFHOEFAOFEZOF BZEOFBZE " + cutNumber);
		// System.out.println("@" + n.getID() + "" + N);
		// Node c = Network.get(CommonState.r.nextInt(Network.size()));
		// subscribe(n, c);
		// }

	}

	/**
	 *
	 * @param node
	 */
	public static void unsubscribe(final Node node) {
		final Scamp current = (Scamp) node.getProtocol(pid);
		throw new RuntimeException("Not yet impl");
	}

	public static void subscribe(final Node s, final Node c) {
		subscribe(s, c, false);
	}

	/**
	 * Trying an alternative lease where you send as much copies as you cut arcs
	 * 
	 * @param s
	 * @param c
	 * @param cutNumber
	 */
	public static boolean alternativeLease(final Node s, final Node c,
			int cutNumber) {
		final Scamp subscriber = (Scamp) s.getProtocol(pid);
		subscriber.in.clear();
		final Scamp contact = (Scamp) c.getProtocol(pid);
		if (subscriber.isUp() && contact.isUp() && contact.degree() > 0) {
			if (subscriber.addNeighbor(c)) {
				contact.in.add(s);
				--cutNumber;
			}
			for (int i = 0; i < cutNumber; ++i) {
				Node n = contact.getPeers().get(i % contact.getPeers().size());
				forward(s, n, new ArrayList<Node>());
			}
			return true;
		}
		return false;
	}

	public static void subscribe(final Node s, final Node c,
			final boolean isLease) {
		final Scamp subscriber = (Scamp) s.getProtocol(pid);
		subscriber.in.clear();
		if (!isLease) {
			subscriber.out.clear();
		}
		final Scamp contact = (Scamp) c.getProtocol(pid);
		// System.err.println("sub @" + c.getID() + " for s=" + s.getID() + "->"
		// + contact + " // " +(isLease ? " T" : "F"));
		if (subscriber.isUp() && contact.isUp()) {

			if (subscriber.addNeighbor(c)) {
				contact.in.add(s);
			}

			for (Node n : contact.getPeers()) {
				forward(s, n, new ArrayList<Node>());
			}
			if (!isLease) {
				for (int i = 0; i < Scamp.c && contact.degree() > 0; i++) {
					Node n = contact.getNeighbor(CommonState.r.nextInt(contact
							.degree()));
					forward(s, n, new ArrayList<Node>());
				}
			}
		} else {
			throw new RuntimeException("@sub:" + s.getID() + "  @con:"
					+ c.getID());
		}
	}

	/**
	 *
	 * @param s
	 * @param node
	 * @param counter
	 * @return
	 */
	public static boolean forward(final Node s, final Node node, List<Node> path) {
		// System.err.println("fwd: s=" + s.getID() + " @"+ node.getID());
		final Scamp N = (Scamp) node.getProtocol(pid);
		if (N.isUp()) {
			path.add(node);
			if (path.size() < FORWARD_TTL) {
				final Scamp current = (Scamp) node.getProtocol(pid);
				if (current.p() && node.getID() != s.getID()
						&& !current.contains(s)) {
					final Scamp subscriber = (Scamp) s.getProtocol(pid);
					// process failure probability
					if (!current.pF(path)) {
						// add it in the partial view
						if (current.addNeighbor(s)) {
							subscriber.in.add(node);
						}
					}
					// System.err.println("subscribed @" + node.getID() +
					// " for s=" + s.getID());
					return true;

				} else if (current.degree() > 0) {
					Node next = current.out.get(CommonState.r.nextInt(current
							.degree()));
					return forward(s, next, path);
				} else {
					System.err.println("DEAD END for subscription " + s.getID()
							+ " @" + node.getID());
					return false;
				}
			} else {
				System.err.println("Forward for " + s.getID() + " timed out @"
						+ node.getID());
				return false;
			}
		}
		return false;
	}

}
