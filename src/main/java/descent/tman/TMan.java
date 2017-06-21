package descent.tman;

import java.util.ArrayList;
import java.util.List;

import descent.rps.IMessage;
import descent.rps.IPeerSampling;
import descent.spray.MergingRegister;
import descent.spray.Spray;
import descent.spray.SprayPartialView;
import peersim.core.Node;

/**
 * Structured overlay builder using a ranking function to converge to the
 * desired topology.
 */
public class TMan extends Spray implements IPeerSampling {

	// #A Configuration from peersim

	// #B Local variables
	public TManPartialView partialViewTMan;
	public IDescriptor descriptor;

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            the peersim configuration
	 */
	public TMan(String prefix) {
		super(prefix);
		// (TODO) make rps configurable
		this.partialViewTMan = new TManPartialView();
		this.descriptor = Descriptor.get();
	}

	/**
	 * Empty constructor
	 */
	public TMan() {
		super();
		this.partialViewTMan = new TManPartialView();
		this.descriptor = Descriptor.get();
	}

	public void periodicCall() {
		super.periodicCall();

		if (!this.isUp) {
			return;
		}

		// #1 Choose a neighbor to exchange with
		Node q = null;
		TMan qTMan = null;
		if (this.partialViewTMan.size() > 0 && this.age % 2 == 0) {
			q = this.partialViewTMan.getRandom();
			qTMan = (TMan) q.getProtocol(TMan.pid);
			if (!qTMan.isUp) {
				this.partialViewTMan.remove(q);
				return;
			}
		} else if (this.partialView.size() > 0) {
			q = this.partialView.getOldest();
			qTMan = (TMan) q.getProtocol(TMan.pid);
			if (!qTMan.isUp) {
				return;
			}
		}

		// #2 Prepare a sample
		List<Node> sample = this.partialViewTMan.getSample(this.node, q, this.partialView.getPeers(),
				Math.floor(this.partialView.size() / 2));
		IMessage result = qTMan.onPeriodicCallTMan(this.node, new TManMessage(sample));
		// #3 Integrate remote sample if it fits better
		this.partialViewTMan.merge(this, this.node, (List<Node>) result.getPayload(), this.partialView.size());
	}

	public IMessage onPeriodicCallTMan(Node origin, IMessage message) {
		// #1 prepare a sample
		List<Node> sample = this.partialViewTMan.getSample(this.node, origin, this.partialView.getPeers(),
				Math.floor(this.partialView.size() / 2));
		// #2 merge the received sample
		this.partialViewTMan.merge(this, this.node, (List<Node>) message.getPayload(), this.partialView.size());
		// #3 send the prepared sample to origin
		return new TManMessage(sample);
	}

	public void join(Node joiner, Node contact) {
		super.join(joiner, contact);

		this.partialViewTMan.clear();

		if (this.node == null) {
			this.node = joiner;
		}

		if (contact != null) {
			this.addNeighborTMan(contact);
			TMan contactTMan = (TMan) contact.getProtocol(TMan.pid);
			contactTMan.onSubscriptionTMan(this.node);
		}
		this.isUp = true;
	}

	public void onSubscriptionTMan(Node origin) {
		List<Node> aliveNeighbors = this.getAliveNeighbors();
		if (aliveNeighbors.size() > 0) {
			List<Node> sample = new ArrayList<Node>();
			sample.add(origin);
			for (Node neighbor : aliveNeighbors) {
				TMan neighborTMan = (TMan) neighbor.getProtocol(TMan.pid);
				neighborTMan.addNeighborTMan(origin);
			}
		} else {
			this.addNeighborTMan(origin);
		}
	}

	public void leave() {
		this.isUp = false;
		this.partialViewTMan.clear();
	}

	@Override
	public IPeerSampling clone() {
		TMan tmanClone = new TMan();
		try {
			tmanClone.partialView = (SprayPartialView) this.partialView.clone();
			tmanClone.register = (MergingRegister) this.register.clone();
			tmanClone.partialViewTMan = (TManPartialView) this.partialViewTMan.clone();
			tmanClone.descriptor = Descriptor.get();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return tmanClone;
	}

	public boolean addNeighborTMan(Node peer) {
		if (!this.node.equals(peer)) {
			List<Node> sample = new ArrayList<Node>();
			sample.add(peer);
			this.partialViewTMan.merge(this, this.node, sample, this.partialView.size());
			return this.partialViewTMan.contains(peer);
		} else {
			return false;
		}
	}

}
