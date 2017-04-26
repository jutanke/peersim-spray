package descent.tman;

import java.util.List;

import descent.rps.APeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IPeerSampling;
import descent.spray.Spray;
import peersim.core.Node;

/**
 * Structured overlay builder using a ranking function to converge to the
 * desired topology.
 */
public class TMan extends APeerSamplingProtocol implements IPeerSampling {

	// #A Configuration from peersim

	// #B Local variables
	public TManPartialView partialView;
	public Spray rps;
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
		this.rps = new Spray(prefix);
		this.partialView = new TManPartialView();
		this.descriptor = Descriptor.get();
	}

	/**
	 * Empty constructor
	 */
	public TMan() {
		super();
		this.rps = new Spray();
		this.partialView = new TManPartialView();
		this.descriptor = Descriptor.get();
	}

	public void periodicCall() {
		if (!this.isUp) {
			return;
		}

		// #1 Choose a neighbor to exchange with
		Node q = null;
		TMan qTMan = null;
		if (this.partialView.size() > 0) {
			q = this.partialView.getRandom();
			qTMan = (TMan) q.getProtocol(TMan.pid);
			if (!qTMan.isUp) {
				this.partialView.remove(q);
				return;
			}
		} else if (this.rps.partialView.size() > 0) {
			q = this.rps.partialView.getOldest();
			qTMan = (TMan) q.getProtocol(TMan.pid);
			if (!qTMan.isUp) {
				return;
			}
		}

		// #2 Prepare a sample
		List<Node> sample = this.partialView.getSample(qTMan, Math.floor(this.rps.partialView.size() / 2));
		
	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	public void join(Node joiner, Node contact) {
		// TODO Auto-generated method stub

	}

	public void onSubscription(Node origin) {
		// TODO Auto-generated method stub

	}

	public void leave() {
		// TODO Auto-generated method stub

	}

	public List<Node> getPeers(int k) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPeerSampling clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addNeighbor(Node peer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean pFail(List<Node> path) {
		// TODO Auto-generated method stub
		return false;
	}

}
