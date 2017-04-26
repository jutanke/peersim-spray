package descent.spray;

import java.util.List;

import descent.rps.APeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IPeerSampling;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

/**
 * Random peer-sampling protocol that self-adapts its functioning to the size of
 * the network using local knowledge only.
 */
public class Spray extends APeerSamplingProtocol implements IPeerSampling {

	// #A Configuration from peersim
	// In average, the number of arcs should be ~ a*ln(N)+b
	private static final String PAR_A = "a";
	private static Double A = 1.;

	private static final String PAR_B = "b";
	private static Double B = 0.;

	// #B Local variables
	public SprayPartialView partialView;

	public MergingRegister register;

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            the peersim configuration
	 */
	public Spray(String prefix) {
		super(prefix);
		this.partialView = new SprayPartialView();
		this.register = new MergingRegister();

		Spray.A = Configuration.getDouble(prefix + "." + Spray.PAR_A, Spray.A);
		Spray.B = Configuration.getDouble(prefix + "." + Spray.PAR_B, Spray.B);
	}

	/**
	 * Empty constructor
	 */
	public Spray() {
		super();
		this.partialView = new SprayPartialView();
		this.register = new MergingRegister();
	}

	@Override
	protected boolean pFail(List<Node> path) {
		// The probability is constant since the number of hops to establish
		// a connection is constant: p1 -> bridge -> p2 -> bridge -> p1
		double pf = 1 - Math.pow(1 - APeerSamplingProtocol.fail, 6);
		return CommonState.r.nextDouble() < pf;
	}

	public void periodicCall() {
		if (!this.isUp || this.partialView.size() <= 0) {
			return;
		}
		// #1 Choose the peer to exchange with
		this.partialView.incrementAge();
		Node q = this.partialView.getOldest();
		Spray qSpray = (Spray) q.getProtocol(APeerSamplingProtocol.pid);
		// #A Peer is down: departed or left
		if (!qSpray.isUp) {
			this.onPeerDown(q);
			return;
		}
		// #B Arc did not properly established
		boolean isFailedConnection = this.pFail(null);
		if (isFailedConnection) {
			this.onArcDown(q);
			return;
		}

		// #2 Create a sample
		List<Node> sample = this.partialView.getSample(this.node, q, true);
		IMessage received = qSpray.onPeriodicCall(this.node,
				new SprayMessage(sample, this.register.networks, this.register.size, this.partialView.size()));
		// #3 Check if must merge networks
		this.onMerge(this.register.isMerge((SprayMessage) received, this.partialView.size()), q);
		// #4 Merge the received sample with current partial view
		List<Node> samplePrime = (List<Node>) received.getPayload();
		this.partialView.mergeSample(this.node, q, samplePrime, sample, true);

	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		List<Node> samplePrime = this.partialView.getSample(this.node, origin, false);
		// #0 Check there is a network merging in progress
		this.onMerge(this.register.isMerge((SprayMessage) message, this.partialView.size()), origin);
		// #1 Process the sample to send back
		this.partialView.mergeSample(this.node, origin, (List<Node>) message.getPayload(), samplePrime, false);
		// #2 Prepare the result to send back
		SprayMessage result = new SprayMessage(samplePrime, this.register.networks, this.register.size,
				this.partialView.size());
		return result;
	}

	public void join(Node joiner, Node contact) {
		// #0 make sure the partial view starts empty
		this.partialView.clear();
		// #1 Lazy loading of the node identity
		if (this.node == null) {
			this.node = joiner;
		}
		// #2 Check if it is the very first peer
		if (contact != null) {
			// #A Inject A arcs to expect A*ln(N)+B arcs
			this.inject(Spray.A, 0., contact);
			// #B Inform the contact peer
			Spray contactSpray = (Spray) contact.getProtocol(Spray.pid);
			contactSpray.onSubscription(this.node);
		}
		this.isUp = true;
	}

	public void onSubscription(Node origin) {
		// #0 Check dead neighbors
		List<Node> deadNeighbors = this.getDeadNeighbors();
		for (Node deadNeighbor : deadNeighbors) {
			this.onPeerDown(deadNeighbor);
		}
		// #1 Forward subscription to neighbors
		List<Node> aliveNeighbors = this.getAliveNeighbors();
		if (aliveNeighbors.size() > 0) {
			// #A If the contact peer has neighbors
			for (Node neighbor : aliveNeighbors) {
				Spray neighborSpray = (Spray) neighbor.getProtocol(Spray.pid);
				neighborSpray.addNeighbor(origin);
			}
		} else {
			// #B Otherwise it keeps this neighbor: 2-peers network
			// #3 Inject A + B arcs to expect A*ln(N)+B arcs
			this.inject(Spray.A, Spray.B, origin);
		}
	}

	/**
	 * Inject A+B number of arcs.
	 * 
	 * @param A
	 *            A*ln(N)
	 * @param B
	 *            +B
	 */
	private void inject(Double A, Double B, Node neighbor) {
		Double a = A;
		for (Integer i = 0; i < Math.floor(A); ++i) {
			this.addNeighbor(neighbor);
			a -= 1;
		}
		if (CommonState.r.nextDouble() < a) {
			this.addNeighbor(neighbor);
		}
		Double b = B;
		for (Integer i = 0; i < Math.floor(B); ++i) {
			this.addNeighbor(neighbor);
			b -= 1;
		}
		if (CommonState.r.nextDouble() < b) {
			this.addNeighbor(neighbor);
		}
	}

	public void leave() {
		this.isUp = false;
		this.partialView.clear();
	}

	public List<Node> getPeers(int k) {
		return this.partialView.getPeers(k);
	}

	@Override
	public IPeerSampling clone() {
		try {
			Spray sprayClone = new Spray();
			sprayClone.partialView = (SprayPartialView) this.partialView.clone();
			sprayClone.register = (MergingRegister) this.register.clone();
			return sprayClone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean addNeighbor(Node peer) {
		return this.partialView.addNeighbor(peer);
	}

	/**
	 * Procedure that handles the peer failures when detected
	 * 
	 * @param q
	 *            the peer supposedly crashed or departed
	 */
	private void onPeerDown(Node q) {
		// #1 Probability to *not* recreate the connection: A/ln(N) + B/N
		double pRemove = Spray.A / this.partialView.size() + Spray.B / Math.exp(this.partialView.size());
		// double pRemove = 1. / this.partialView.size();
		// #2 Remove all occurrences of q in our partial view and count them
		int occ = this.partialView.removeAll(q);
		// int occ = 1;
		// this.partialView.removeNode(q);
		if (this.partialView.size() > 0) {
			// #3 probabilistically double known connections
			for (int i = 0; i < occ; ++i) {
				if (CommonState.r.nextDouble() > pRemove) {
					Node toDouble = this.partialView.getLowestOcc();
					this.partialView.addNeighbor(toDouble);
				}
			}
		}
	}

	/**
	 * Replace an failed arc by an existing one
	 * 
	 * @param q
	 *            the destination of the arc to replace
	 */
	private void onArcDown(Node q) {
		// #1 Remove the unestablished link
		this.partialView.removeNode(q);
		// #2 Double a known connection at random
		if (this.partialView.size() > 0) {
			Node toDouble = this.partialView.getLowestOcc();
			this.partialView.addNeighbor(toDouble);
		}
	}

	private void onMerge(Double ratio, Node sender) {
		// #1 process the mandatory added arc when its over 1
		while (ratio >= 1) {
			// this.addNeighbor(getNeighbor(CommonState.r.nextInt(this.partialView
			// .size()))); // sp_*
			this.addNeighbor(sender); // spr_*
			--ratio;
		}
		// #2 process the random arcs
		if (CommonState.r.nextDouble() < ratio) {
			this.addNeighbor(getNeighbor(CommonState.r.nextInt(this.partialView.size()))); // sp_*
		}
	}
}
