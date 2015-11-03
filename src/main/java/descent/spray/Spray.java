package descent.spray;

import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IRandomPeerSampling;

/**
 * The Spray protocol
 */
public class Spray extends ARandomPeerSamplingProtocol implements
		IRandomPeerSampling {

	// #A no configuration needed, everything is adaptive
	// #B no values from the configuration file of peersim
	// #C local variables
	public SprayPartialView partialView;

	public MergingRegister register;

	/**
	 * Constructor of the Spray instance
	 * 
	 * @param prefix
	 *            the peersim configuration
	 */
	public Spray(String prefix) {
		super(prefix);
		this.partialView = new SprayPartialView();
		this.register = new MergingRegister();
	}

	public Spray() {
		this.partialView = new SprayPartialView();
		this.register = new MergingRegister();
	}

	@Override
	protected boolean pFail(List<Node> path) {
		// the probability is constant since the number of hops to establish
		// a connection is constant
		double pf = 1 - Math.pow(1 - ARandomPeerSamplingProtocol.fail, 6);
		return CommonState.r.nextDouble() < pf;
	}

	public void periodicCall() {
		if (this.isUp && this.partialView.size() > 0) {
			// #1 choose the peer to exchange with
			this.partialView.incrementAge();
			Node q = this.partialView.getOldest();
			Spray qSpray = (Spray) q
					.getProtocol(ARandomPeerSamplingProtocol.pid);
			boolean isFailedConnection = this.pFail(null);
			if (qSpray.isUp() && !isFailedConnection) {
				// #A if the chosen peer is alive, exchange
				List<Node> sample = this.partialView.getSample(this.node, q,
						true);
				IMessage received = qSpray.onPeriodicCall(this.node,
						new SprayMessage(sample, this.register.networks,
								this.register.size, this.partialView.size()));
				// #1 check if must merge networks
				if (CommonState.r.nextDouble() < this.register.isMerge(
						(SprayMessage) received, this.partialView.size())) {
					this.onMerge(q);
				}
				// #2 merge the received sample with current partial view
				List<Node> samplePrime = (List<Node>) received.getPayload();
				this.partialView.mergeSample(this.node, q, samplePrime, sample,
						true);
			} else {
				// #B run the appropriate procedure
				if (!qSpray.isUp()) {
					this.onPeerDown(q);
				} else if (isFailedConnection) {
					this.onArcDown(q);
				}
			}
		}
	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		List<Node> samplePrime = this.partialView.getSample(this.node, origin,
				false);
		// #2 check there is a network merging in progress
		if (CommonState.r.nextDouble() < this.register.isMerge(
				(SprayMessage) message, this.partialView.size())) {
			this.onMerge(origin);
		}
		// #0 process the sample to send back
		this.partialView.mergeSample(this.node, origin,
				(List<Node>) message.getPayload(), samplePrime, false);
		// #1 prepare the result to send back
		SprayMessage result = new SprayMessage(samplePrime,
				this.register.networks, this.register.size,
				this.partialView.size());

		return result;
	}

	public void join(Node joiner, Node contact) {
		if (this.node == null) { // lazy loading of the node identity
			this.node = joiner;
		}
		if (contact != null) { // the very first join does not have any contact
			Spray contactSpray = (Spray) contact.getProtocol(Spray.pid);
			this.partialView.clear();
			this.partialView.addNeighbor(contact);
			contactSpray.onSubscription(this.node);
		}
		this.isUp = true;
	}

	public void onSubscription(Node origin) {
		List<Node> aliveNeighbors = this.getAliveNeighbors();
		if (aliveNeighbors.size() > 0) {
			// #1 if the contact peer has neighbors
			for (Node neighbor : aliveNeighbors) {
				Spray neighborSpray = (Spray) neighbor.getProtocol(Spray.pid);
				neighborSpray.addNeighbor(origin);
			}
		} else {
			// #2 otherwise it takes the advertisement for itself
			this.addNeighbor(origin);
		}
	}

	public void leave() {
		this.isUp = false;
		this.partialView.clear();
		// nothing else
	}

	public List<Node> getPeers(int k) {
		return this.partialView.getPeers(k);
	}

	@Override
	public IRandomPeerSampling clone() {
		try {
			Spray sprayClone = new Spray();
			sprayClone.partialView = (SprayPartialView) this.partialView
					.clone();
			sprayClone.register = (MergingRegister) this.register.clone();
			return sprayClone;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
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
	 *            the peer supposedly crashed
	 */
	private void onPeerDown(Node q) {
		// #1 probability to NOT recreate the connection
		double pRemove = 1.0 / this.partialView.size();
		// #2 remove all occurrences of q in our partial view and count them
		int occ = this.partialView.removeAll(q);
		if (this.partialView.size() > 0) {
			// #3 probabilistically double known connections
			for (int i = 0; i < occ; ++i) {
				if (CommonState.r.nextDouble() > pRemove) {
					Node toDouble = this.partialView.getPeers().get(
							CommonState.r.nextInt(this.partialView.size()));
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
		// #1 remove the unestablished link
		this.partialView.removeNode(q);
		// #2 double a known connection at random
		if (this.partialView.size() > 0) {
			Node toDouble = this.partialView.getPeers().get(
					CommonState.r.nextInt(this.partialView.size()));
			this.partialView.addNeighbor(toDouble);
		}
	}

	private void onMerge(Node sender) {
		this.addNeighbor(getNeighbor(CommonState.r.nextInt(this.partialView
				.size()))); // sp_*
		// this.addNeighbor(sender); // spr_*
	}
}
