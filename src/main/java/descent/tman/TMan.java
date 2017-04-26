package descent.tman;

import java.util.ArrayList;
import java.util.List;

import descent.rps.IMessage;
import descent.rps.IPeerSampling;
import descent.spray.Spray;
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
		if (this.partialViewTMan.size() > 0) {
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
		List<Node> sample = this.partialViewTMan.getSample(qTMan, this.partialView.getPeers(),
				Math.floor(this.partialView.size() / 2));
		IMessage result = qTMan.onPeriodicCall(this.node, new TManMessage(sample));
		// #3 Integrate remote sample if it fits better
		this.partialViewTMan.merge(this, (List<Node>) result.getPayload(), this.partialView.size());
	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		// #1 prepare a sample
		TMan originTMan = (TMan) origin.getProtocol(TMan.pid);
		List<Node> sample = this.partialViewTMan.getSample(originTMan, this.partialView.getPeers(),
				Math.floor(this.partialView.size() / 2));
		// #2 merge the received sample
		this.partialViewTMan.merge(this, sample, this.partialView.size());
		// #3 send the prepared sample to origin
		return new TManMessage(sample);
	}

	public void join(Node joiner, Node contact) {
		this.partialViewTMan.clear();

		if (this.node == null) {
			this.node = joiner;
		}

		if (contact != null) {
			this.addNeighbor(contact);
			TMan contactTMan = (TMan) contact.getProtocol(TMan.pid);
			contactTMan.onSubscription(this.node);
		}
		this.isUp = true;
	}

	public void onSubscription(Node origin) {
		List<Node> aliveNeighbors = this.getAliveNeighbors();
		if (aliveNeighbors.size() > 0) {
			List<Node> sample = new ArrayList<Node>();
			sample.add(origin);
			for (Node neighbor : aliveNeighbors) {
				TMan neighborTMan = (TMan) neighbor.getProtocol(TMan.pid);
				neighborTMan.addNeighbor(origin);
			}
		} else {
			this.addNeighbor(origin);
		}
	}

	public void leave() {
		this.isUp = false;
		this.partialViewTMan.clear();
	}

	public List<Node> getPeers(int k) {
		// (TODO)
		return null;
	}

	@Override
	public IPeerSampling clone() {
		TMan tmanClone = (TMan) super.clone();
		tmanClone.partialViewTMan = (TManPartialView) this.partialViewTMan.clone();
		tmanClone.descriptor = new Descriptor((Descriptor) this.descriptor);
		return tmanClone;
	}

	@Override
	public boolean addNeighbor(Node peer) {
		List<Node> sample = new ArrayList<Node>();
		sample.add(peer);
		this.partialViewTMan.merge(this, sample, this.partialView.size());
		return this.partialViewTMan.contains(peer);
	}

	@Override
	protected boolean pFail(List<Node> path) {
		// (TODO)
		return false;
	}

}
