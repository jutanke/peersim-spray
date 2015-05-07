package descent.scamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import peersim.core.CommonState;
import peersim.core.Node;
import descent.controllers.DynamicNetwork;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IRandomPeerSampling;

/**
 * The Scamp protocol. ATTENTION: The periodic protocol (or "lease") suggested
 * in the Paper "Peer-to-Peer Membership Management for Gossip-Based Protocols"
 * from A.J. Ganesh, A.-M. Kermarrec and L. Massoulié seems to be broken as the
 * arc count grows until the network is almost complete.
 *
 * Original implementation at: https://github.com/csko/Peersim/tree/master/scamp
 *
 */
public class Scamp extends ARandomPeerSamplingProtocol implements
		IRandomPeerSampling {

	// #A no specific parameter to set for Scamp
	// #B specific variables
	// #C local variables
	PartialView partialView;
	PartialView inView;

	/**
	 * Constructor of Scamp
	 * 
	 * @param prefix
	 *            the peersim configuration
	 */
	public Scamp(String prefix) {
		super(prefix);
		this.partialView = new PartialView();
		this.inView = new PartialView();
	}

	public Scamp() {
		super();
		this.partialView = new PartialView();
		this.inView = new PartialView();
	}

	@Override
	protected boolean pFail(List<Node> path) {
		// #1 compute the shortest path from the beginning to the end, i.e.,
		// remove the cycles
		// #A create a graph with the path in argument
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
		// #B breadth-first search to compute the shortest path
		HashSet<Long> discovered = new HashSet<Long>();
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
		// #2 compute the failure probability
		// #2A a round-trip using the whole path (worst case)
		double pHighest = 1 - Math.pow(1 - ARandomPeerSamplingProtocol.fail,
				Math.pow(path.size(), 2) + 3 * path.size() + 2);
		// #2B a round-trip using the shortest path (best case)
		double pLowest = 1 - Math.pow(1 - ARandomPeerSamplingProtocol.fail,
				Math.pow(minHops, 2) + 3 * minHops + 2);
		// #2C a round trip using the shortest path and single travel trough
		// the other peers in the path (precise case)
		double pPrecise = 1 - Math.pow(1 - ARandomPeerSamplingProtocol.fail,
				Math.pow(minHops, 2) + 3 * minHops + 2
						+ (path.size() - minHops));
		return CommonState.r.nextDouble() < pLowest;
	}

	public void periodicCall() {
		// #1 remove the connection to us and count them
		int occ = this.inView.size();
		for (Node in : this.inView.getPeers()) {
			Scamp inScamp = (Scamp) in.getProtocol(Scamp.pid);
			inScamp.partialView.removeNode(this.node);
		}
		// #2 no one knows us anymore
		this.inView.clear();
		// #3 re-subscribe to the network
		Node rNeighbor;
		Scamp rNeighborScamp;
		if (this.partialView.size() > 0) {
			// #3A get a peer from the neighborhood
			rNeighbor = this.partialView.getPeers(1).get(0);

		} else {
			// #3B get a random alive peer from the network
			rNeighbor = DynamicNetwork.getNode();
		}
		if (occ != 0) {
			// #3C ask it to spread the subscription
			rNeighborScamp = (Scamp) rNeighbor.getProtocol(Scamp.pid);
			rNeighborScamp.onPeriodicCall(this.node, new ScampMessage(occ));
		} else {
			// #3D all is lost, leave
			this.leave();
			// this.join(this.node, rNeighbor);
		}

	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		// #0 if the peer has no partial view, he has permission to accept it
		if (this.partialView.size() == 0) {
			this.onForwardedSubscription(origin, new ArrayList<Node>());
		} else {
			int toSpray = (Integer) message.getPayload();
			// #1 get the alive neighborhood of this peer
			List<Node> neighbors = this.partialView.getPeers(Integer.MAX_VALUE);
			Collections.shuffle(neighbors);
			for (int i = 0; i < toSpray; ++i) {
				Scamp neighborScamp = (Scamp) neighbors.get(
						i % neighbors.size()).getProtocol(Scamp.pid);
				neighborScamp.onForwardedSubscription(origin,
						new ArrayList<Node>());
			}
		}
		return null;
	}

	public void join(Node joiner, Node contact) {
		this.isUp = true;
		if (this.node == null) { // lazy loading of the node identity
			this.node = joiner;
		}
		if (contact != null) { // if its not the very first node of the network
			// add the contact node in the partial view of the origin
			this.addNeighbor(contact);
			Scamp contactScamp = (Scamp) contact.getProtocol(Scamp.pid);
			contactScamp.onSubscription(this.node);
		}
	}

	public void onSubscription(Node origin) {
		// #1 forward the subscription to peers in the network
		for (Node neighbor : this.partialView.getPeers(Integer.MAX_VALUE)) {
			Scamp neighborScamp = (Scamp) neighbor.getProtocol(Scamp.pid);
			neighborScamp
					.onForwardedSubscription(origin, new ArrayList<Node>());
		}
	}

	/**
	 * Function representing an event triggered when a peer received a forwarded
	 * subscription. Then, the peer can accept the subscription or forward it to
	 * a random neighbor
	 * 
	 * @param origin
	 *            the peer that subscribes
	 * @param path
	 *            the path traveled by the subscription
	 */
	private void onForwardedSubscription(Node origin, List<Node> path) {
		if (!this.isUp()) {
			return;
		} // silently stop the forwarding #1
		if (this.node.getID() == origin.getID() && this.partialView.size() == 0) {
			return;
		} // silently stop the forwarding #2

		path.add(this.node); // add ourself to the path
		if (this.node.getID() != origin.getID()
				&& !this.partialView.contains(origin) && this.pAccept()) {
			// #A add origin to the partial view, and "this.node" to the
			// origin's in view
			this.addNeighbor(origin);
		} else {
			// #B otherwise, choose a neighbor at random and forward the subs
			Node neighbor = this.partialView.getPeers().get(
					CommonState.r.nextInt(this.partialView.size()));
			Scamp neighborScamp = (Scamp) neighbor.getProtocol(Scamp.pid);
			neighborScamp.onForwardedSubscription(origin, path);
		}
	}

	/**
	 * Check if the subscription should be accepted
	 * 
	 * @return true if the subscription get accepted, false otherwise
	 */
	private boolean pAccept() {
		return CommonState.r.nextDouble() < 1.0 / (1.0 + this.partialView
				.size());
	}

	// Leave without giving notice
	public void leave() {
		// #1 clear the inview and outview
		// #A clear the partial view of other peers to simulate the lease
		for (Node in : this.inView.getPeers()) {
			Scamp inScamp = (Scamp) in.getProtocol(Scamp.pid);
			inScamp.partialView.removeNode(this.node);
		}
		this.inView.clear();
		// #B clear the in view of other peers to simulate the timeout on
		// heartbeat
		for (Node out : this.partialView.getPeers()) {
			Scamp outScamp = (Scamp) out.getProtocol(Scamp.pid);
			outScamp.inView.removeNode(this.node);
		}
		this.partialView.clear();
		this.isUp = false;
	}

	public List<Node> getPeers(int k) {
		return this.partialView.getPeers(k);
	}

	@Override
	public IRandomPeerSampling clone() {
		try {
			Scamp scampClone = new Scamp();
			scampClone.partialView = (PartialView) this.partialView.clone();
			scampClone.inView = (PartialView) this.inView.clone();
			return scampClone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean addNeighbor(Node peer) {
		boolean added = false;
		if (this.node.getID() != peer.getID()
				&& this.partialView.addNeighbor(peer)) {
			Scamp peerScamp = (Scamp) peer.getProtocol(Scamp.pid);
			peerScamp.inView.addNeighbor(this.node);
			added = true;
		}
		return added;
	}

}
