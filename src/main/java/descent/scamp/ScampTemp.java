package descent.scamp;

import java.util.Collections;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IRandomPeerSampling;

/**
 * The Scamp protocol. ATTENTION: The periodic protocol (or "lease") suggested
 * in the Paper "Peer-to-Peer Membership Management for Gossip-Based Protocols"
 * from A.J. Ganesh, A.-M. Kermarrec and L. Massoulie seems to be broken as the
 * arc count grows until the network is almost complete
 *
 * Original implementation at: https://github.com/csko/Peersim/tree/master/scamp
 *
 */
public class ScampTemp extends ARandomPeerSamplingProtocol implements
		IRandomPeerSampling {

	// #A no specific parameter to set for Scamp
	// #B no specific variable to set for Scamp
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
		if (this.isUp && this.degree() > 0) {
			// #1 remove the connection to us and count them
			int occ = this.inView.size();
			for (Node in : this.inView.getPeers()) {
				ScampTemp inScamp = (ScampTemp) in.getProtocol(ScampTemp.pid);
				inScamp.partialView.removeNode(this.node);
			}
			// #2 no one knows us anymore
			this.inView.clear();
			// #3 re-subscribe to the network
			// #3A get an alive peer
			List<Node> aliveNeighbors = this.getAliveNeighbors();
			Node randomNeighbor = aliveNeighbors.get(CommonState.r
					.nextInt(aliveNeighbors.size()));
			ScampTemp randomNeighborScamp = (ScampTemp) randomNeighbor
					.getProtocol(ScampTemp.pid);
			// #3B ask it to spread the subscription
			randomNeighborScamp.onPeriodicCall(this.node, new ScampTempMessage(
					occ));
		}
	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		int toSpray = (Integer) message.getPayload();
		// #1 get the alive neighborhood of this peer
		List<Node> aliveNeighbors = this.getAliveNeighbors();
		Collections.shuffle(aliveNeighbors);
		for (int i = 0; i < toSpray; ++i) {
			// (TODO)
		}
		return null;
	}

	public void join(Node joiner, Node contact) {
		// (TODO)

	}

	public void onSubscription(Node origin) {
		// TODO Auto-generated method stub

	}

	private void onForwardSubscription(Node origin){
		// TODO
	}
	
	public void leave() {
		this.inView.clear();
		this.partialView.clear();
		this.isUp = false;
		// Other peer may still point at it
	}

	public List<Node> getPeers(int k) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

}
