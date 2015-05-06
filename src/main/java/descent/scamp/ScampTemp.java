package descent.scamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
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
public class ScampTemp extends ARandomPeerSamplingProtocol implements
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
	public ScampTemp(String prefix) {
		super(prefix);
		this.partialView = new PartialView();
		this.inView = new PartialView();
	}

	public ScampTemp() {
		super();
		this.partialView = new PartialView();
		this.inView = new PartialView();
	}

	public void periodicCall() {
		// #1 remove the connection to us and count them
		int occ = this.inView.size();
		for (Node in : this.inView.getPeers()) {
			ScampTemp inScamp = (ScampTemp) in.getProtocol(ScampTemp.pid);
			inScamp.partialView.removeNode(this.node);
		}
		// #2 no one knows us anymore
		this.inView.clear();
		// #3 re-subscribe to the network
		Node rNeighbor;
		ScampTemp rNeighborScamp;
		if (this.partialView.size() > 0) {
			// #3A get a peer from the neighborhood
			rNeighbor = this.partialView.getPeers(1).get(0);
			rNeighborScamp = (ScampTemp) rNeighbor.getProtocol(ScampTemp.pid);
		} else {
			// #3B get a peer from the network
			rNeighbor = Network.get(CommonState.r.nextInt(Network.size()));
			rNeighborScamp = (ScampTemp) rNeighbor.getProtocol(ScampTemp.pid);
			while (!rNeighborScamp.isUp()) {
				rNeighbor = Network.get(CommonState.r.nextInt(Network.size()));
				rNeighborScamp = (ScampTemp) rNeighbor
						.getProtocol(ScampTemp.pid);
			}
		}
		if (occ == 0) {
			this.leave();
			this.join(this.node, rNeighbor);
			return;
		}
		// #3C ask it to spread the subscription
		rNeighborScamp.onPeriodicCall(this.node, new ScampTempMessage(occ));
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
				ScampTemp neighborScamp = (ScampTemp) neighbors.get(
						i % neighbors.size()).getProtocol(ScampTemp.pid);
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
			ScampTemp contactScamp = (ScampTemp) contact
					.getProtocol(ScampTemp.pid);
			contactScamp.onSubscription(this.node);
		}
	}

	public void onSubscription(Node origin) {
		// #1 forward the subscription to peers in the network
		for (Node neighbor : this.partialView.getPeers(Integer.MAX_VALUE)) {
			ScampTemp neighborScamp = (ScampTemp) neighbor
					.getProtocol(ScampTemp.pid);
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
			ScampTemp neighborScamp = (ScampTemp) neighbor
					.getProtocol(ScampTemp.pid);
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
			ScampTemp inScamp = (ScampTemp) in.getProtocol(ScampTemp.pid);
			inScamp.partialView.removeNode(this.node);
		}
		this.inView.clear();
		// #B clear the in view of other peers to simulate the timeout on
		// heartbeat
		for (Node out : this.partialView.getPeers()) {
			ScampTemp outScamp = (ScampTemp) out.getProtocol(ScampTemp.pid);
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
			ScampTemp scampClone = new ScampTemp();
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
			ScampTemp peerScamp = (ScampTemp) peer.getProtocol(ScampTemp.pid);
			peerScamp.inView.addNeighbor(this.node);
			added = true;
		}
		return added;
	}

}
