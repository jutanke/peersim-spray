package descent.spray;

import java.util.HashSet;
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

	public HashSet<Integer> from; // network id before the merge
	public Integer remember; // pv size before the merge
	public HashSet<Integer> to; // network id after the merge

	public boolean mustMerge = false;

	/**
	 * Constructor of the Spray instance
	 * 
	 * @param prefix
	 *            the peersim configuration
	 */
	public Spray(String prefix) {
		super(prefix);
		this.partialView = new SprayPartialView();
		this.from = new HashSet<Integer>();
		this.remember = -1;
		this.to = new HashSet<Integer>();
	}

	public Spray() {
		this.partialView = new SprayPartialView();
		this.from = new HashSet<Integer>();
		this.remember = -1;
		this.to = new HashSet<Integer>();
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
						new SprayMessage(sample, this.from, this.remember,
								this.to));
				// #1 check if must merge networks
				this.isFutureMerge((SprayMessage) received);
				if (this.isMerge((SprayMessage) received)) {
					this.onMerge((SprayMessage) received, q);
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
		this.isFutureMerge((SprayMessage) message);
		if (this.isMerge((SprayMessage) message)) {
			this.onMerge((SprayMessage) message, origin);
		}
		// #0 process the sample to send back
		this.partialView.mergeSample(this.node, origin,
				(List<Node>) message.getPayload(), samplePrime, false);
		// #1 prepare the result to send back
		SprayMessage result = new SprayMessage(samplePrime, this.from,
				this.remember, this.to);

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
			sprayClone.from = (HashSet<Integer>) this.from.clone();
			sprayClone.remember = new Integer(this.remember);
			sprayClone.to = (HashSet<Integer>) this.to.clone();
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

	public void isFutureMerge(SprayMessage m) {
		// #1 check if there is a new merging process currently running
		boolean isAlreadyMergeWithNetwork = this.to.containsAll(m.getTo());
		if (!isAlreadyMergeWithNetwork) {
			// #A save the fact that the peer must process a merge asap
			this.mustMerge = true;
			// #B save informations
			this.from = this.to;
			this.remember = new Integer(this.partialView.size());
			this.to = (HashSet<Integer>) this.to.clone();
			this.to.addAll(m.getTo());
		}
	}

	/**
	 * Check if the received message should lead to a merge of networks
	 * 
	 * @param m
	 *            the received message
	 * @return true if it is a merge, false otherwise
	 */
	public boolean isMerge(SprayMessage m) {
		HashSet<Integer> thisFrom = this.from;
		HashSet<Integer> mFrom = m.getFrom();
		// #1 handle the first merge of network
		if (this.from.isEmpty()) {
			thisFrom = this.to;
		}
		if (m.getFrom().isEmpty()) {
			mFrom = m.getTo();
		}
		// #2 check if the sender and receiver of the message come from the
		// same network. In such case, don't merge
		boolean comeFromTheSameNetwork = (this.to.containsAll(mFrom) && mFrom
				.containsAll(this.to))
				|| (thisFrom.containsAll(m.getTo()) && m.getTo().containsAll(
						thisFrom))
				|| (thisFrom.containsAll(mFrom) && mFrom.containsAll(thisFrom)
						&& this.to.containsAll(m.getTo()) && this.from
							.containsAll(m.getFrom()));
		return this.mustMerge && !comeFromTheSameNetwork;
	}

	/**
	 * Process the probability to add an arc knowing there is a merge
	 * 
	 * @param m
	 *            the received message
	 * @param sender
	 *            the node that created the message
	 * @return true if it adds an arc, false otherwise
	 */
	private boolean onMerge(SprayMessage m, Node sender) {
		List<Node> sampleReceived = (List<Node>) m.getPayload();
		// #0 reset the must merge value
		this.mustMerge = false;
		// #1 process the relative difference between sizes of networks
		double diff = Math.abs(this.partialView.size() - sampleReceived.size()
				* 2 - 0.5); // -0.5 because of the ceiled sent value
		if (m.getRemember() != -1) {
			diff = Math.abs(this.remember - m.getRemember());
		}
		// #2 process probability of create duplicate
		// #A ratio between the network sizes
		double ratio = 1 / (Math.exp(diff) + 1);
		double p = (ratio - 1) * Math.log(1 - ratio) - ratio * Math.log(ratio);
		boolean duplicate = false;
		// #B create an entry on the senders
		if (CommonState.r.nextDouble() < p) {
			duplicate = true;
			// this.partialView.addNeighbor(getNeighbor(CommonState.r.nextInt(this.partialView.size())
			// )); // sp_*
			this.partialView.addNeighbor(sender); // spr_*
		}
		return duplicate;
	}
}
